package com.professorqu.helpers;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.professorqu.saving.RecipeResult;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Pair;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

public class Generator {
    private final OrtEnvironment environment = OrtEnvironment.getEnvironment();
    private OrtSession session;
    private final int seed;

    private static final Random RNG = new Random();

    /**
     * Create a new Generator
     * @param server the Minecraft server where the Generator is run
     */
    public Generator(MinecraftServer server) {
        this.seed = Generator.convertSeed(server.getOverworld().getSeed());

        this.loadModel();
    }

    /**
     * Load the model
     */
    private void loadModel() {
        // Get the resource stream
        URL stream = Generator.class.getResource("/assets/model.onnx");
        if (stream == null) return;

        byte[] modelBytes;
        try {
            modelBytes = IOUtils.toByteArray(stream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load model");
        }

        try {
            this.session = this.environment.createSession(modelBytes);
        } catch (OrtException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create session");
        }
    }

    /**
     * Convert seed from long to int
     * @param longSeed the seed as long
     * @return the seed as int scaled to less than a million
     */
    private static int convertSeed(long longSeed) {
        int seed = (int) longSeed;
        while (Math.abs(seed) > 1_000_000) {
            seed /= 2;
        }

        return seed;
    }

    /**
     * Generate a new crafting recipe result from the given input from the AI model.
     * If the model generates a disabled item, generate a random one and save it instead.
     * @param recipe the recipe input to check
     * @param enabledFeatures the features that are enabled in the world
     * @return the result of the recipe
     */
    public Pair<RecipeResult, Boolean> generate(int[] recipe, FeatureSet enabledFeatures) {
        float[][] input = this.convertInput(recipe);
        float[] output;

        try {
            OnnxTensor tensor = OnnxTensor.createTensor(this.environment, input);
            Map<String, OnnxTensor> inputTensor = Map.of("l_x_", tensor);

            OrtSession.Result result = this.session.run(inputTensor);

            output = ((float[][]) result.get(0).getValue())[0];
        } catch (OrtException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate");
        }

        RecipeResult result = RecipeResult.createRecipeResult(output[0], output[1]);
        boolean saveResult = false;

        // If the item is randomly generated, save the result
        while (!Registries.ITEM.get(result.itemId()).isEnabled(enabledFeatures)) {
            result = result.withItemId(getRandomItemId());
            saveResult = true;
        }

        return new Pair<>(result, saveResult);
    }

    /**
     * Convert input from int array to input of neural network
     * @param recipe the recipe to convert
     * @return the input for a tensor
     */
    private float[][] convertInput(int[] recipe) {
        float[] recipeFloat = new float[recipe.length + 1];
        for (int i = 0; i < recipe.length; i++) {
            recipeFloat[i] = (float) recipe[i];
        }
        recipeFloat[recipe.length] = this.seed;

        return new float[][]{recipeFloat};
    }

    /**
     * Get a random item id
     * @return an item id
     */
    private static int getRandomItemId() {
        return RNG.nextInt(Registries.ITEM.size());
    }
}

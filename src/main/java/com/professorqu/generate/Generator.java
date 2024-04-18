package com.professorqu.generate;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.professorqu.saving.RecipeResult;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
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

    public Generator(MinecraftServer server) {
        int seed = (int) server.getOverworld().getSeed();
        while (Math.abs(seed) > 1_000_000) {
            seed /= 2;
        }

        this.seed = seed;

        URL stream = Generator.class.getResource("/assets/model.onnx");
        if (stream == null) return;

        byte[] modelBytes;
        try {
            modelBytes = IOUtils.toByteArray(stream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load model");
        }

        OrtEnvironment env = OrtEnvironment.getEnvironment();
        try {
            this.session = env.createSession(modelBytes);
        } catch (OrtException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create session");
        }
    }

    /**
     * Generate a new crafting recipe result from the given input
     * @param recipe the recipe input to check
     * @param enabledFeatures the features that are enabled in the world
     * @return the result of the recipe
     */
    public RecipeResult generate(int[] recipe, FeatureSet enabledFeatures) {
        float[] recipeFloat = new float[recipe.length + 1];
        for (int i = 0; i < recipe.length; i++) {
            recipeFloat[i] = (float) recipe[i];
        }
        recipeFloat[recipe.length] = this.seed;

        float[][] input = {recipeFloat};
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

        RecipeResult result = new RecipeResult(output[0], output[1]);

        while (!Registries.ITEM.get(result.getItemId()).isEnabled(enabledFeatures)) {
            result.setItemId(getRandomItemId());
        }

        return result;
    }

    private static int getRandomItemId() {
        return RNG.nextInt(Registries.ITEM.size());
    }
}

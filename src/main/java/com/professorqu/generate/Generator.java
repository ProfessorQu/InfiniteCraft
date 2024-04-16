package com.professorqu.generate;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.professorqu.InfiniteCraft;
import com.professorqu.saving.RecipeResult;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

public class Generator {
    private final OrtEnvironment environment = OrtEnvironment.getEnvironment();
    private OrtSession session;

    private static final Generator INSTANCE = new Generator();
    private static final Random RNG = new Random();

    private void loadSession() {
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
            session = env.createSession(modelBytes);
        } catch (OrtException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create session");
        }
    }

    private Generator() {
        this.loadSession();
    }

    /**
     * Generate a new crafting recipe result from the given input
     * @param recipe the recipe input to check
     * @param enabledFeatures the features that are enabled in the world
     * @return the result of the recipe
     */
    public static RecipeResult generate(int[] recipe, int seed, FeatureSet enabledFeatures) {
        float[] recipeFloat = new float[recipe.length + 1];
        for (int i = 0; i < recipe.length; i++) {
            recipeFloat[i] = (float) recipe[i];
        }
        recipeFloat[recipe.length] = seed;

        float[][] input = {recipeFloat};
        float[] output;

        try {
            OnnxTensor tensor = OnnxTensor.createTensor(INSTANCE.environment, input);
            Map<String, OnnxTensor> inputTensor = Map.of("l_x_", tensor);

            OrtSession.Result result = INSTANCE.session.run(inputTensor);

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

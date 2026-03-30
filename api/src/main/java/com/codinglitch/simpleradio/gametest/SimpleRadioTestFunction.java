package com.codinglitch.simpleradio.gametest;

import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class SimpleRadioTestFunction {
    public static final Map<String, SimpleRadioTestFunction> FUNCTIONS = new HashMap<>();

    public final String id;
    public final String name;
    public final TestFunction func;

    protected SimpleRadioTestFunction(
            String id, String name, String batch, String template, Rotation rot, int maxTicks, long setupTicks,
            boolean required, int requiredSuccesses, int maxAttempts, Consumer<GameTestHelper> consumer
    ) {
        this.func = new TestFunction(batch, name, template, rot, maxTicks, setupTicks, required, false, maxAttempts, requiredSuccesses, true, consumer);
        this.id = id;
        this.name = name;

        FUNCTIONS.put(id, this);
    }

    public static Collection<TestFunction> from(Class<?> clazz) {
        List<TestFunction> functions = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {

            SimpleRadioTestFunction func = SimpleRadioTestFunction.from(clazz, method);
            if (func == null) continue;

            functions.add(func.func);
        }

        return functions;
    }

    @Nullable
    public static SimpleRadioTestFunction from(Class<?> clazz, Method method) {

        TestHolder holder = clazz.getAnnotation(TestHolder.class);

        GameTest gameTest = method.getAnnotation(GameTest.class);
        TestAlteration alteration = method.getAnnotation(TestAlteration.class);
        if (gameTest == null) return null;

        Consumer<GameTestHelper> consumer = helper -> {
            try {
                method.invoke(null, new SimpleRadioTestHelper(helper));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };

        String id = alteration == null ? "" : alteration.id();
        if (id.isEmpty()) id = clazz.getName() + "." + method.getName();

        String holderName = alteration == null ? "" : holder.namespace();
        if (holderName.isEmpty()) holderName = clazz.getSimpleName();

        String name = alteration == null ? "" : alteration.name();
        if (name.isEmpty()) name = holderName + "." + method.getName();

        String templateDir = alteration == null ? "" : alteration.templateDir();
        if (templateDir.isEmpty()) templateDir = "gametest/" + holder.dir();

        String template = ("%s:%s/%s").formatted(holder.namespace(), templateDir, gameTest.template());

        return new SimpleRadioTestFunction(
                id, name, gameTest.batch(), template,
                StructureUtils.getRotationForRotationSteps(gameTest.rotationSteps()),
                gameTest.timeoutTicks(), gameTest.setupTicks(),
                gameTest.required(), gameTest.requiredSuccesses(), gameTest.attempts(),
                consumer
        );
    }
}

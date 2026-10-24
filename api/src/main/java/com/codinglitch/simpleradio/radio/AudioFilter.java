package com.codinglitch.simpleradio.radio;

/**
 * An audio filter to be used for audio processing.
 */
public abstract class AudioFilter {
    public record Chain(float[] audio, AudioEffect ...effects) {
        /**
         * Applies the effect chain to the provided data.
         * @param data The audio data to apply the effect to
         * @return The processed data
         */
        public short[] process(short[] data) {
            // convert to an array of floats first for the audio effects
            int length = data.length;

            float[] audioData = audio;
            if (length != 960) audioData = new float[length];

            for (int i = 0; i < length; i++) audioData[i] = data[i] / 32768f;

            for (AudioEffect effect : effects) effect.process(audioData);

            // convert it back to shorts
            for (int i = 0; i < length; i++) {
                float s = Math.max(-1f, Math.min(1f, audioData[i]));
                data[i] = (short) (s * 32767f);
            }

            return data;
        }

        public <E extends AudioEffect> E get(Class<E> type) {
            for (AudioEffect effect : effects) {
                if (effect.getClass().equals(type)) return (E) effect;
            }
            return null;
        }
    }

    private float severity;
    public float volume;

    public float getSeverity() { return severity; }
    public void setSeverity(float severity) { this.severity = severity; }

    public short[] apply(short[] data) {
        for (int i = 0; i < data.length; i++) {
            short audio = data[i];
            data[i] = (short) (audio * volume);
        }

        return data;
    }
}

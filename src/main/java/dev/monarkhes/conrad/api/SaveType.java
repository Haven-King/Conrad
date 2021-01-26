package dev.monarkhes.conrad.api;

/**
 * Determines how this config option should be saved. Required on all config objects.
 */
public enum SaveType {
	/**
	 * A config with a {@code SaveType} of {@code USER} will be available on the client at all times. It will be
	 * available on the server only if {@link Options#synced()} on the class or {@link Value.Options#synced()}
	 * on the method for an individual config value return {@code true}.
	 *
	 * {@code USER} configs are saved on the client, in the root "config" folder of the Minecraft installation.
	 */
	USER,

	/**
	 * A config with a {@code SaveType} of {@code LEVEL} will be available on the logical server at all times.
	 * It will be available on the client only if either {@link Options#synced()} on the class or
	 * {@link Value.Options#synced()} on the method of an individual config value return {@code true}.
	 *
	 * {@code LEVEL} configs are saved in a few different places:
	 * <ul>
	 *     <li>The root "config" folder of the Minecraft client installation contains default config values. These
	 *     are used when creating a new world, and can be modified by accessing Mod Menu from the main menu.</li>
	 *     <li>The "config" folder of a world will contain the {@code LEVEL} configs for that particular save.
	 *     They can be modified by accessing Mod Menu while playing on a single player world.</li>
	 *     <li>The "config" folder of a dedicated Minecraft server instance contains the config values for that
	 *     server. These can be modified by a level 4 operator by accessing Mod Menu while connected to the server.
	 *     </li>
	 * </ul>
	 */
	LEVEL
}

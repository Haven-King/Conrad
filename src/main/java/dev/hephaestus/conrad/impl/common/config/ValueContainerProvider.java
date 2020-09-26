package dev.hephaestus.conrad.impl.common.config;

public interface ValueContainerProvider {
	ValueContainerProvider ROOT = new ValueContainerProvider() {
		@Override
		public ValueContainer getValueContainer() {
			return ValueContainer.ROOT;
		}

		@Override
		public PlayerValueContainers getPlayerValueContainers() {
			return null;
		}
	};

	ValueContainer getValueContainer();
	PlayerValueContainers getPlayerValueContainers();
}

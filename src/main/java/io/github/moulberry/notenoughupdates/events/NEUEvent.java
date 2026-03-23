package io.github.moulberry.notenoughupdates.events;

public class NEUEvent {
	private boolean cancelled = false;

	public boolean isCancelable() {
		return false;
	}

	public boolean isCanceled() {
		return cancelled;
	}

	public void setCanceled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public void cancel() {
		setCanceled(true);
	}

	public boolean post() {
		NEUEventBus.INSTANCE.post(this);
		return isCancelable() && isCanceled();
	}
}

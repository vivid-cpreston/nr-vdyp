package ca.bc.gov.nrs.vdyp.model;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class VdypPolygon extends BaseVdypPolygon<VdypLayer, Float, VdypSpecies, VdypSite> {

	private final Optional<Integer> targetYear;

	public VdypPolygon(
			PolygonIdentifier polygonIdentifier, Float percentAvailable, String fiz, BecDefinition bec,
			Optional<PolygonMode> modeFip
	) {
		this(polygonIdentifier, percentAvailable, fiz, bec, modeFip, Optional.empty());
	}

	public VdypPolygon(
			PolygonIdentifier polygonIdentifier, Float percentAvailable, String fiz, BecDefinition bec,
			Optional<PolygonMode> modeFip, Optional<Integer> targetYear
	) {
		super(polygonIdentifier, percentAvailable, fiz, bec, modeFip);

		this.targetYear = targetYear;
	}

	/**
	 * Copy constructs from the simple attributes of another polygon, but does not copy layers.
	 *
	 * @param <O>                     Type of the polygon to copy
	 * @param <U>                     Type of percent available in the other polygon
	 * @param toCopy                  The polygon to copy
	 * @param convertPercentAvailable Function to convert
	 */
	public <O extends BaseVdypPolygon<?, U, ?, ?>, U> VdypPolygon(
			O toCopy, Function<U, Float> convertPercentAvailable
	) {
		super(toCopy, convertPercentAvailable);
		if (toCopy instanceof VdypPolygon vdypPolygonToCopy) {
			this.targetYear = vdypPolygonToCopy.getTargetYear();
		} else {
			this.targetYear = Optional.empty();
		}
	}

	public Optional<Integer> getTargetYear() {
		return targetYear;
	}

	/**
	 * Accepts a configuration function that accepts a builder to configure.
	 *
	 * <pre>
	 * VdypPolygon myPolygon = VdypPolygon.build(builder-&gt; {
			builder.polygonIdentifier(polygonId);
			builder.percentAvailable(percentAvailable);
	 * })
	 * </pre>
	 *
	 * @param config The configuration function
	 * @return The object built by the configured builder.
	 * @throws IllegalStateException if any required properties have not been set by the configuration function.
	 */
	public static VdypPolygon build(Consumer<Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	public static class Builder extends
			BaseVdypPolygon.Builder<VdypPolygon, VdypLayer, Float, VdypSpecies, VdypSite, VdypLayer.Builder, VdypSpecies.Builder, VdypSite.Builder> {

		protected Optional<Integer> targetYear = Optional.empty();

		@Override
		protected VdypPolygon doBuild() {
			return new VdypPolygon(
					polygonIdentifier.get(), percentAvailable.get(), forestInventoryZone.get(),
					biogeoclimaticZone.get(), mode, targetYear
			);
		}

		@Override
		protected VdypLayer.Builder getLayerBuilder() {
			return new VdypLayer.Builder();
		}

		public void targetYear(Optional<Integer> targetYear) {
			this.targetYear = targetYear;
		}
	}
}

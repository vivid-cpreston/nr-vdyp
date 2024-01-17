package ca.bc.gov.nrs.vdyp.forward;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.forward.model.VdypSpeciesUtilization;
import ca.bc.gov.nrs.vdyp.io.EndOfRecord;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.AbstractStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.ControlMapValueReplacer;
import ca.bc.gov.nrs.vdyp.io.parse.GroupingStreamingParser;
import ca.bc.gov.nrs.vdyp.io.parse.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.StreamingParserFactory;
import ca.bc.gov.nrs.vdyp.io.parse.ValueParser;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

public class VdypLayerByUtilizationParser
		implements ControlMapValueReplacer<StreamingParserFactory<Collection<VdypSpeciesUtilization>>, String> {

	public static final String CONTROL_KEY = "VDYP_SPECIES";

	private static final String DESCRIPTION = "DESCRIPTION"; // POLYDESC
	private static final String LAYER_TYPE = "LAYER_TYPE"; // LAYERG
	private static final String GENUS_INDEX = "SPECIES_INDEX"; // ISP
	private static final String GENUS = "SPECIES"; // SP0
	private static final String UTILIZATION_CLASS_INDEX = "UTILIZATION_CLASS_INDEX"; // J
	private static final String BASAL_AREA = "UTILIZATION_CLASS_INDEX"; // BA
	private static final String LIVE_TREES_PER_HECTARE = "LIVE_TREES_PER_HECTARE"; // TPH
	private static final String LOREY_HEIGHT = "LOREY_HEIGHT"; // LHJ
	private static final String WHOLE_STEM_VOLUME = "WHOLE_STEM_VOLUME"; // VOLWS
	private static final String CLOSE_UTIL_VOLUME = "CLOSE_UTIL_VOLUME"; // VOLCU
	private static final String CU_VOLUME_LESS_DECAY = "CU_VOLUME_LESS_DECAY"; // VOL_D
	private static final String CU_VOLUME_LESS_DECAY_WASTAGE = "CU_VOLUME_LESS_DECAY_WASTAGE"; // VOL_DW
	private static final String CU_VOLUME_LESS_DECAY_WASTAGE_BREAKAGE = "CU_VOLUME_LESS_DECAY_WASTAGE_BREAKAGE"; // VOL_DWB	
	private static final String QUADRATIC_MEAN_DIAMETER_BREAST_HEIGHT = "QUADRATIC_MEAN_DIAMETER_BREAST_HEIGHT"; // DQ	

	@Override
	public String getControlKey() {
		return CONTROL_KEY;
	}

	@Override
	public StreamingParserFactory<Collection<VdypSpeciesUtilization>>
			map(String fileName, FileResolver fileResolver, Map<String, Object> control)
					throws IOException, ResourceParseException {
		return () -> {
			var lineParser = new LineParser()
					.strippedString(25, DESCRIPTION).space(1)
					.value(1, LAYER_TYPE,
							ValueParser.valueOrMarker(
									ValueParser.LAYER, ValueParser.optionalSingleton(
											x -> "".equals(x) || "Z".equals(x), EndOfRecord.END_OF_RECORD)
							)).space(1)
					.value(2, GENUS_INDEX, ValueParser.INTEGER).space(1)
					.value(2, GENUS, ValueParser.GENUS).space(1)
					.value(3, UTILIZATION_CLASS_INDEX, ValueParser.UTILIZATION_CLASS)
					.value(9, BASAL_AREA, ValueParser.FLOAT)
					.value(9, LIVE_TREES_PER_HECTARE, ValueParser.FLOAT)
					.value(9, LOREY_HEIGHT, ValueParser.FLOAT)
					.value(9, WHOLE_STEM_VOLUME, ValueParser.FLOAT)
					.value(9, CLOSE_UTIL_VOLUME, ValueParser.FLOAT)
					.value(9, CU_VOLUME_LESS_DECAY, ValueParser.FLOAT)
					.value(9, CU_VOLUME_LESS_DECAY_WASTAGE, ValueParser.FLOAT)
					.value(9, CU_VOLUME_LESS_DECAY_WASTAGE_BREAKAGE, ValueParser.FLOAT)
					.value(6, QUADRATIC_MEAN_DIAMETER_BREAST_HEIGHT, ValueParser.FLOAT);

			var is = fileResolver.resolve(fileName);

			var delegateStream = new AbstractStreamingParser<ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord>>(
					is, lineParser, control
			) {
				@SuppressWarnings("unchecked")
				@Override
				protected ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord> convert(Map<String, Object> entry)
						throws ResourceParseException {
					
					var polygonId = (String) entry.get(DESCRIPTION);
					var layerType = (ValueOrMarker<Optional<LayerType>, EndOfRecord>) entry.get(LAYER_TYPE);
					var genusIndex = (Integer) entry.get(GENUS_INDEX);
					var genus = (String) entry.get(GENUS);
					var ucIndex = (UtilizationClass) entry.get(UTILIZATION_CLASS_INDEX);
					var basalArea = (Float) entry.get(BASAL_AREA);
					var liveTreesPerHectare = (Float) entry.get(LIVE_TREES_PER_HECTARE);
					var loreyHeight = (Float) entry.get(LOREY_HEIGHT);
					var wholeStemVolume = (Float) entry.get(WHOLE_STEM_VOLUME);
					var closeUtilVolume = (Float) entry.get(CLOSE_UTIL_VOLUME);
					var cuVolumeLessDecay = (Float) entry.get(CU_VOLUME_LESS_DECAY);
					var cuVolumeLessDecayWastage = (Float) entry.get(CU_VOLUME_LESS_DECAY_WASTAGE);
					var cuVolumeLessDecayWastageBreakage = (Float) entry.get(CU_VOLUME_LESS_DECAY_WASTAGE_BREAKAGE);
					var quadraticMean_DBH = (Float) entry.get(QUADRATIC_MEAN_DIAMETER_BREAST_HEIGHT);
					
					var builder = new ValueOrMarker.Builder<Optional<VdypSpeciesUtilization>, EndOfRecord>();
					var result = layerType.handle(l -> {
						
						return builder.value(
								Optional.of(new VdypSpeciesUtilization(polygonId, layerType.getValue().get().get(), genusIndex
										, genus, ucIndex, basalArea, liveTreesPerHectare, loreyHeight
										, wholeStemVolume, closeUtilVolume, cuVolumeLessDecay, cuVolumeLessDecayWastage
										, cuVolumeLessDecayWastageBreakage, quadraticMean_DBH)));
					}, m -> {
						return builder.marker(m);
					});
					
					return result;
				}
			};

			return new GroupingStreamingParser<Collection<VdypSpeciesUtilization>, ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord>>(
					delegateStream
			) {

				@Override
				protected boolean skip(ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord> nextChild) {
					return nextChild.getValue().map(Optional::isEmpty).orElse(false);
				}

				@Override
				protected boolean stop(ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord> nextChild) {
					return nextChild.isMarker();
				}

				@Override
				protected Collection<VdypSpeciesUtilization>
						convert(List<ValueOrMarker<Optional<VdypSpeciesUtilization>, EndOfRecord>> children) {
					return children.stream().map(ValueOrMarker::getValue).map(Optional::get) 
							.flatMap(Optional::stream) // Skip if empty (and unknown layer type)
							.toList();
				}

			};
		};
	}
}

package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2;
import ca.bc.gov.nrs.vdyp.model.MatrixMap2Impl;
import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parses an HL Coefficient data file.
 *
 * These files each contain a complete mapping of species aliases and regions to
 * a (one-based) list of coefficients, and therefore has 32 lines. Each row
 * contains:
 * <ol>
 * <li>(cols 0-1) Species alias</li>
 * <li>(col 3) Region ('I' or 'C')</li>
 * <li>(cols 4-13, 14-23, ...) two - four floats in 10 character fields.
 * </ol>
 * All lines are read; there is no provision for blank lines. There may be
 * multiple lines with the same Species and Region values; the last one wins.
 * <p>
 * The file must populate #Species * #Regions (currently 32) values.
 * <p>
 * The result of the parse is a {@link MatrixMap2} of Coefficients indexed by
 * first species, then region.
 * <p>
 * FIP Control indices: 050, 051, 052
 * <p>
 * Examples: coe/REGYHLP.COE, coe/REGYHLPA.COE, coe/REGYHLPB.DAT (respectively)
 *
 * @author Kevin Smith, Vivid Solutions
 * @see ControlMapSubResourceParser
 */
public class HLCoefficientParser
		implements ControlMapSubResourceParser<MatrixMap2<String, Region, Optional<Coefficients>>> {

	public static final String CONTROL_KEY_P1 = "HL_PRIMARY_SP_EQN_P1";
	public static final String CONTROL_KEY_P2 = "HL_PRIMARY_SP_EQN_P2";
	public static final String CONTROL_KEY_P3 = "HL_PRIMARY_SP_EQN_P3";

	public static final int NUM_COEFFICIENTS_P1 = 3;
	public static final int NUM_COEFFICIENTS_P2 = 2;
	public static final int NUM_COEFFICIENTS_P3 = 4;

	public static final String SP0_KEY = "sp0";
	public static final String REGION_KEY = "region";
	public static final String COEFFICIENT_KEY = "coefficient";

	private String controlKey;

	public HLCoefficientParser(int numCoefficients, String controlKey) {
		super();
		this.lineParser = new LineParser() {

			@Override
			public boolean isStopLine(String line) {
				return line.startsWith("   ");
			}

		}.value(2, SP0_KEY, ValueParser.STRING).space(1).value(1, REGION_KEY, ValueParser.REGION)
				.multiValue(numCoefficients, 10, COEFFICIENT_KEY, ValueParser.FLOAT);
		this.controlKey = controlKey;
	}

	LineParser lineParser;

	@Override
	public MatrixMap2<String, Region, Optional<Coefficients>> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		final var regionIndicies = Arrays.asList(Region.values());
		final var speciesIndicies = GenusDefinitionParser.getSpeciesAliases(control);

		MatrixMap2<String, Region, Optional<Coefficients>> result = new MatrixMap2Impl<>(
				speciesIndicies, regionIndicies, MatrixMap2Impl.emptyDefault()
		);
		lineParser.parse(is, result, (v, r) -> {
			var sp0 = (String) v.get(SP0_KEY);
			var region = (Region) v.get(REGION_KEY);
			@SuppressWarnings("unchecked")
			var coefficients = (List<Float>) v.get(COEFFICIENT_KEY);
			if (!speciesIndicies.contains(sp0)) {
				throw new ValueParseException(sp0, sp0 + " is not a valid species");
			}

			r.put(sp0, region, Optional.of(new Coefficients(coefficients, 1)));

			return r;
		}, control);
		return result;
	}

	@Override
	public String getControlKey() {
		return controlKey;
	}

}

package ca.bc.gov.nrs.vdyp.fip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.BreakageEquationGroupParser;
import ca.bc.gov.nrs.vdyp.io.parse.BreakageParser;
import ca.bc.gov.nrs.vdyp.io.parse.BySpeciesDqCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.CloseUtilVolumeParser;
import ca.bc.gov.nrs.vdyp.io.parse.CoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.ComponentSizeParser;
import ca.bc.gov.nrs.vdyp.io.parse.ControlFileParser;
import ca.bc.gov.nrs.vdyp.io.parse.ControlMapModifier;
import ca.bc.gov.nrs.vdyp.io.parse.DecayEquationGroupParser;
import ca.bc.gov.nrs.vdyp.io.parse.DefaultEquationNumberParser;
import ca.bc.gov.nrs.vdyp.io.parse.EquationModifierParser;
import ca.bc.gov.nrs.vdyp.io.parse.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.HLCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.HLNonprimaryCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.SiteCurveAgeMaximumParser;
import ca.bc.gov.nrs.vdyp.io.parse.SiteCurveParser;
import ca.bc.gov.nrs.vdyp.io.parse.SmallComponentBaseAreaParser;
import ca.bc.gov.nrs.vdyp.io.parse.SmallComponentDQParser;
import ca.bc.gov.nrs.vdyp.io.parse.SmallComponentHLParser;
import ca.bc.gov.nrs.vdyp.io.parse.SmallComponentProbabilityParser;
import ca.bc.gov.nrs.vdyp.io.parse.SmallComponentWSVolumeParser;
import ca.bc.gov.nrs.vdyp.io.parse.StockingClassFactorParser;
import ca.bc.gov.nrs.vdyp.io.parse.TotalStandWholeStemParser;
import ca.bc.gov.nrs.vdyp.io.parse.UpperCoefficientParser;
import ca.bc.gov.nrs.vdyp.io.parse.UtilComponentBaseAreaParser;
import ca.bc.gov.nrs.vdyp.io.parse.UtilComponentDQParser;
import ca.bc.gov.nrs.vdyp.io.parse.UtilComponentWSVolumeParser;
import ca.bc.gov.nrs.vdyp.io.parse.ValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.VeteranBQParser;
import ca.bc.gov.nrs.vdyp.io.parse.VeteranDQParser;
import ca.bc.gov.nrs.vdyp.io.parse.VeteranLayerVolumeAdjustParser;
import ca.bc.gov.nrs.vdyp.io.parse.VolumeEquationGroupParser;
import ca.bc.gov.nrs.vdyp.io.parse.VolumeNetDecayParser;
import ca.bc.gov.nrs.vdyp.io.parse.VolumeNetDecayWasteParser;
import ca.bc.gov.nrs.vdyp.model.JProgram;

/**
 * Parser for FIP control files
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class FipControlParser {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FipControlParser.class);

	static final ValueParser<String> FILENAME = String::strip;

	public static final String MINIMUM_HEIGHT = "MINIMUM_HEIGHT";
	public static final String MINIMUM_BASE_AREA = "MINIMUM_BASE_AREA";
	public static final String MINIMUM_PREDICTED_BASE_AREA = "MINIMUM_PREDICTED_BASE_AREA";
	public static final String MINIMUM_VETERAN_HEIGHT = "MINIMUM_VETERAN_HEIGHT";

	public static final float DEFAULT_MINIMUM_VETERAN_HEIGHT = 10.0f;

	ControlFileParser controlParser = new ControlFileParser() //
			.record(ControlKey.MAX_NUM_POLY, ValueParser.INTEGER)

			.record(ControlKey.BEC_DEF, FILENAME) // RD_BECD
			.record(ControlKey.SP0_DEF, FILENAME) // RD_SP0

			.record(ControlKey.FIP_YIELD_POLY_INPUT, FILENAME) // GET_FIPP
			.record(ControlKey.FIP_YIELD_LAYER_INPUT, FILENAME) // GET_FIPL
			.record(ControlKey.FIP_YIELD_LX_SP0_INPUT, FILENAME) // GET_FIPS

			.record(ControlKey.VDYP_POLYGON, FILENAME) //
			.record(ControlKey.VDYP_LAYER_BY_SPECIES, FILENAME) //
			.record(ControlKey.VDYP_LAYER_BY_SP0_BY_UTIL, FILENAME) //

			.record(ControlKey.VOLUME_EQN_GROUPS, FILENAME) // RD_VGRP
			.record(ControlKey.DECAY_GROUPS, FILENAME) // RD_DGRP
			.record(ControlKey.BREAKAGE_GROUPS, FILENAME) // RD_BGRP IPSJF157

			.record(ControlKey.SITE_CURVE_NUMBERS, ValueParser.optional(FILENAME)) // RD_E025
			.record(ControlKey.SITE_CURVE_AGE_MAX, ValueParser.optional(FILENAME)) // RD_E026

			.record(ControlKey.DEFAULT_EQ_NUM, FILENAME) // RD_GRBA1
			.record(ControlKey.EQN_MODIFIERS, FILENAME) // RD_GMBA1
			.record(ControlKey.STOCKING_CLASS_FACTORS, FILENAME) // RD_STK33

			.record(ControlKey.COE_BA, FILENAME) // RD_E040 IPSJF128
			.record(ControlKey.COE_DQ, FILENAME) // RD_E041 IPSJF129
			.record(ControlKey.UPPER_BA_BY_CI_S0_P, FILENAME) // RD_E043 IPSJF128

			.record(ControlKey.HL_PRIMARY_SP_EQN_P1, FILENAME) // RD_YHL1
			.record(ControlKey.HL_PRIMARY_SP_EQN_P2, FILENAME) // RD_YHL2
			.record(ControlKey.HL_PRIMARY_SP_EQN_P3, FILENAME) // RD_YHL3
			.record(ControlKey.HL_NONPRIMARY, FILENAME) // RD_YHL4

			.record(ControlKey.BY_SPECIES_DQ, FILENAME) // RD_E060 IPFJF125
			.record(ControlKey.SPECIES_COMPONENT_SIZE_LIMIT, FILENAME) // RD_E061 IPSJF158

			.record(ControlKey.UTIL_COMP_BA, FILENAME) // RD_UBA1
			.record(ControlKey.UTIL_COMP_DQ, FILENAME) // RD_UDQ1

			.record(ControlKey.SMALL_COMP_PROBABILITY, FILENAME) // RD_SBA1
			.record(ControlKey.SMALL_COMP_BA, FILENAME) // RD_SBA2
			.record(ControlKey.SMALL_COMP_DQ, FILENAME) // RD_SDQ1
			.record(ControlKey.SMALL_COMP_HL, FILENAME) // RD_SHL1
			.record(ControlKey.SMALL_COMP_WS_VOLUME, FILENAME) // RD_SVT1

			.record(ControlKey.TOTAL_STAND_WHOLE_STEM_VOL, FILENAME) // RD_YVT1 IPSJF117
			.record(ControlKey.UTIL_COMP_WS_VOLUME, FILENAME) // RD_YVT2 IPSJF121
			.record(ControlKey.CLOSE_UTIL_VOLUME, FILENAME) // RD_YVC1 IPSJF122
			.record(ControlKey.VOLUME_NET_DECAY, FILENAME) // RD_YVD1 IPSJF123
			.record(ControlKey.VOLUME_NET_DECAY_WASTE, FILENAME) // RD_YVW1 IPSJF123
			.record(ControlKey.BREAKAGE, FILENAME) // RD_EMP95 IPSJF157

			.record(ControlKey.VETERAN_LAYER_VOLUME_ADJUST, FILENAME) // RD_YVET
			.record(ControlKey.VETERAN_LAYER_DQ, FILENAME) // RD_YDQV
			.record(ControlKey.VETERAN_BQ, FILENAME) // RD_E098

			.record(
					ControlKey.MINIMA,
					ValueParser.toMap(
							ValueParser.list(ValueParser.FLOAT),
							Collections.singletonMap(MINIMUM_VETERAN_HEIGHT, 10.0f), MINIMUM_HEIGHT, MINIMUM_BASE_AREA,
							MINIMUM_PREDICTED_BASE_AREA, MINIMUM_VETERAN_HEIGHT
					)
			)

			.record(ControlKey.MODIFIER_FILE, ValueParser.optional(FILENAME)) // RD_E198 IPSJF155, XII

			.record(ControlKey.DEBUG_SWITCHES, ValueParser.list(ValueParser.INTEGER)) // IPSJF155
	/*
	 * Debug switches (25) 0=default See IPSJF155, App IX 1st: 1: Do NOT apply BA
	 * limits from SEQ043 2nd: 1: Do NOT apply DQ limits from SEQ043 4th: Future
	 * Development. Choice of upper limits 9th: 0: Normal - Suppress MATH77 error
	 * messages. 1: show some MATH77 errors; 2: show all. 22nd 1: extra preference
	 * for preferred sp (SEQ 010).
	 */

	;

	JProgram jprogram = JProgram.FIP_START; // FIPSTART only TODO Track this down

	public FipControlParser() {

	}

	List<ControlMapModifier> DATA_FILES = Arrays.asList(

			// V7O_FIP
			new FipPolygonParser(),

			// V7O_FIL
			new FipLayerParser(),

			// V7O_FIS
			new FipSpeciesParser()
	);

	List<ControlMapModifier> BASIC_DEFINITIONS = Arrays.asList(

			// RD_BEC
			new BecDefinitionParser(),

			// DEF_BEC
			// Superseded by BecLookup.getGrowthBecs

			// RD_SP0
			new GenusDefinitionParser()
	);

	List<ControlMapModifier> GROUP_DEFINITIONS = Arrays.asList(

			// RD_VGRP
			new VolumeEquationGroupParser(),

			// RD_DGRP
			new DecayEquationGroupParser(),

			// RD_BGRP
			new BreakageEquationGroupParser(),

			// RD_GRBA1
			new DefaultEquationNumberParser(),

			// RD_GMBA1
			new EquationModifierParser()
	);

	List<ControlMapModifier> FIPSTART_ONLY = Arrays.asList(

			// RD_STK33
			new StockingClassFactorParser()

	// TODO minima?
	/*
	 * READ(CNTRV(197), 197, ERR= 912 ) FMINH, FMINBA, FMINBAF,FMINVetH IF (FMINVetH
	 * .le. 0.0) FMINVetH=10.0
	 */
	);
	List<ControlMapModifier> SITE_CURVES = Arrays.asList(

			// User-assigned SC's (Site Curve Numbers)
			//
			// RD_E025
			new SiteCurveParser(),

			// Max tot ages to apply site curves (by SC)
			//
			// RD_E026
			new SiteCurveAgeMaximumParser()
	);

	List<ControlMapModifier> COEFFICIENTS = Arrays.asList(
			// RD_E040
			new CoefficientParser(ControlKey.COE_BA),

			// RD_E041
			new CoefficientParser(ControlKey.COE_DQ),

			// RD_E043
			new UpperCoefficientParser(),

			// RD_YHL1
			new HLCoefficientParser(HLCoefficientParser.NUM_COEFFICIENTS_P1, ControlKey.HL_PRIMARY_SP_EQN_P1),

			// RD_YHL2
			new HLCoefficientParser(HLCoefficientParser.NUM_COEFFICIENTS_P2, ControlKey.HL_PRIMARY_SP_EQN_P2),

			// RD_YHL3
			new HLCoefficientParser(HLCoefficientParser.NUM_COEFFICIENTS_P3, ControlKey.HL_PRIMARY_SP_EQN_P3),

			// RD_YHL4
			new HLNonprimaryCoefficientParser(),

			// RD_E060
			new BySpeciesDqCoefficientParser(),

			// Min and max DQ by species

			// RD_E061
			new ComponentSizeParser(),

			// RD_UBA1
			new UtilComponentBaseAreaParser(),

			// RD_UDQ1
			new UtilComponentDQParser(),

			// Small Component (4.5 to 7.5 cm)

			// RD_SBA1
			new SmallComponentProbabilityParser(),

			// RD_SBA2
			new SmallComponentBaseAreaParser(),

			// RD_SDQ1
			new SmallComponentDQParser(),

			// RD_SHL1
			new SmallComponentHLParser(),

			// RD_SVT1
			new SmallComponentWSVolumeParser(),

			// Standard Volume Relationships

			// RD_YVT1
			new TotalStandWholeStemParser(),

			// RD_YVT2
			new UtilComponentWSVolumeParser(),

			// RD_YVC1
			new CloseUtilVolumeParser(),

			// RD_YVD1
			new VolumeNetDecayParser(),

			// RD_YVW1
			new VolumeNetDecayWasteParser(),

			// RD_E095
			new BreakageParser(),

			// Veterans

			// RD_YVVET
			new VeteranLayerVolumeAdjustParser(),

			// RD_YDQV
			new VeteranDQParser(),

			// RD_E098
			new VeteranBQParser()
	);

	List<ControlMapModifier> ADDITIONAL_MODIFIERS = Arrays.asList(

			// RD_E198
			new ModifierParser(jprogram)
	);

	private void
			applyModifiers(Map<String, Object> control, List<ControlMapModifier> modifiers, FileResolver fileResolver)
					throws ResourceParseException, IOException {
		for (var modifier : modifiers) {
			modifier.modify(control, fileResolver);
		}
	}

	public Map<String, Object> parse(InputStream is, FileResolver fileResolver, Map<String, Object> map)
			throws IOException, ResourceParseException {
		return parse(List.of(is), fileResolver, map);
	}

	public Map<String, Object> parse(List<InputStream> resources, FileResolver fileResolver, Map<String, Object> map)
			throws IOException, ResourceParseException {

		for (var is : resources) {
			map.putAll(controlParser.parse(is, map));
		}

		applyModifiers(map, BASIC_DEFINITIONS, fileResolver);

		// Read Groups

		applyModifiers(map, GROUP_DEFINITIONS, fileResolver);

		// Initialize data file parser factories

		applyModifiers(map, DATA_FILES, fileResolver);

		if (jprogram == JProgram.FIP_START) {
			applyModifiers(map, FIPSTART_ONLY, fileResolver);
		}

		applyModifiers(map, SITE_CURVES, fileResolver);

		// Coeff for Empirical relationships

		applyModifiers(map, COEFFICIENTS, fileResolver);

		// Initiation items NOT for FIPSTART
		if (jprogram != JProgram.FIP_START) {

			throw new UnsupportedOperationException();
			// RD_E106
			// TODO

			// RD_E107
			// TODO

			// RD_E108
			// TODO

			// Minima again, differently?
			// TODO

			/*
			 * READ(CNTRV(197), 197, ERR= 912 ) VMINH, VMINBA, VMINBAeqn,VMINvetH IF
			 * (VMINVetH .le. 0.0) VMINVetH=10.0
			 */

			// RD_E112
			// Was commented out in Fortran

			// RD_E116
			// Was commented out in Fortran
		}

		// Modifiers, IPSJF155-Appendix XII

		// RD_E198
		applyModifiers(map, ADDITIONAL_MODIFIERS, fileResolver);

		// Debug switches (normally zero)
		// TODO
		/*
		 * 141 FORMAT(25I2) DO 140 I=1,25 if ((NDEBUG(I) .ne.0) .and. (IU_WRITE .gt. 0))
		 * & WRITE(IU_WRITE,142) I, NDEBUG(I) 142 FORMAT(' CTR Line 199 has
		 * NDEBUG(',i2,') =',I3) 140 CONTINUE
		 *
		 * if (isDBG) then call dbgMsg( logr, 0 ) "Debug Switches (Ctl Line 199)" ) call
		 * dbgI4( logr, 6 ) " 1: Applying BA Limits               ", NDEBUG( 1) ) call
		 * dbgMsg( logr, 6 ) "       0 - Apply BA limits from SEQ043." ) call dbgMsg(
		 * logr, 6 ) "       1 - Do not apply BA limits from SEQ043." ) call dbgI4(
		 * logr, 6 ) " 2: Applying BA Limits               ", NDEBUG( 2) ) call dbgMsg(
		 * logr, 6 ) "       0 - Apply DQ limits from SEQ043." ) call dbgMsg( logr, 6 )
		 * "       1 - Do not apply DQ limits from SEQ043." ) call dbgI4( logr, 6 )
		 * " 3: Not used                         ", NDEBUG( 3) ) call dbgI4( logr, 6 )
		 * " 4: Not implemented. Upper bounds    ", NDEBUG( 4) ) call dbgMsg( logr, 6 )
		 * "       0 - Will default to (2)." ) call dbgMsg( logr, 6 )
		 * "       1 - Limits from Ctl Line 108 (GRPBA1)." ) call dbgMsg( logr, 6 )
		 * "       2 - Limits from Ctl Line 043 (Coast/Interior)" ) call dbgI4( logr, 6
		 * ) " 5: Not used                         ", NDEBUG( 5) ) call dbgI4( logr, 6 )
		 * " 6: Not used                         ", NDEBUG( 6) ) call dbgI4( logr, 6 )
		 * " 7: Not used                         ", NDEBUG( 7) ) call dbgI4( logr, 6 )
		 * " 8: Not used                         ", NDEBUG( 8) ) call dbgI4( logr, 6 )
		 * " 9: Error handling                   ", NDEBUG( 9) ) call dbgMsg( logr, 6 )
		 * "       0 - Stop on all errors." ) call dbgMsg( logr, 6 )
		 * "       1 - Continue to next poly (non-I/O errs)." ) call dbgI4( logr, 6 )
		 * "10: Not used                         ", NDEBUG(10) ) call dbgI4( logr, 6 )
		 * "11: Not used                         ", NDEBUG(11) ) call dbgI4( logr, 6 )
		 * "12: Not used                         ", NDEBUG(12) ) call dbgI4( logr, 6 )
		 * "13: Not used                         ", NDEBUG(13) ) call dbgI4( logr, 6 )
		 * "14: Not used                         ", NDEBUG(14) ) call dbgI4( logr, 6 )
		 * "15: Not used                         ", NDEBUG(15) ) call dbgI4( logr, 6 )
		 * "16: Not used                         ", NDEBUG(16) ) call dbgI4( logr, 6 )
		 * "17: Not used                         ", NDEBUG(17) ) call dbgI4( logr, 6 )
		 * "18: Not used                         ", NDEBUG(18) ) call dbgI4( logr, 6 )
		 * "19: Not used                         ", NDEBUG(19) ) call dbgI4( logr, 6 )
		 * "20: Not used                         ", NDEBUG(20) ) call dbgI4( logr, 6 )
		 * "21: Not used                         ", NDEBUG(21) ) call dbgI4( logr, 6 )
		 * "22: Determining primary species      ", NDEBUG(22) ) call dbgMsg( logr, 6 )
		 * "       0 - Normal " ) call dbgMsg( logr, 6 )
		 * "       1 - Extra preference for preferred sp. as primary" ) call dbgI4(
		 * logr, 6 ) "23: Not used                         ", NDEBUG(23) ) call dbgI4(
		 * logr, 6 ) "24: Not used                         ", NDEBUG(24) ) call dbgI4(
		 * logr, 6 ) "25: Not used                         ", NDEBUG(25) ) end if
		 */

		return map;
	}

}

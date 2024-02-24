package ca.bc.gov.nrs.vdyp.io.parse.control;

import java.util.Map;

import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

/**
 * Replace a control map entry referencing a file with a parsed version of that
 * file if it was specified, otherwise initialize that entry with a default.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 */
public interface OptionalControlMapSubResourceParser<T>
		extends ControlMapSubResourceParser<T>, OptionalResourceControlMapModifier {

	@Override
	default void defaultModify(Map<String, Object> control) {
		control.put(getControlKeyName(), defaultResult());
	}

	/**
	 * The default value
	 *
	 * @return
	 */
	T defaultResult();

	@Override
	default ValueParser<? extends Object> getValueParser() {
		return OptionalResourceControlMapModifier.super.getValueParser();
	}

}

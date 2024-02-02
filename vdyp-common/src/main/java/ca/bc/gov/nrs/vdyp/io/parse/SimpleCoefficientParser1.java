package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.model.Coefficients;
import ca.bc.gov.nrs.vdyp.model.MatrixMap;
import ca.bc.gov.nrs.vdyp.model.MatrixMapImpl;

public class SimpleCoefficientParser1<K1> implements ControlMapSubResourceParser<Map<K1, Coefficients>> {

	private int indexFrom;

	private BaseCoefficientParser<Coefficients, Coefficients, MatrixMap<Coefficients>> delegate = new BaseCoefficientParser<Coefficients, Coefficients, MatrixMap<Coefficients>>(
			1, null
	) {

		@SuppressWarnings("unchecked")
		@Override
		protected MatrixMap<Coefficients> createMap(List<Collection<?>> keyRanges) {
			return new MatrixMapImpl<>(k -> getCoefficients(), (Collection<K1>) keyRanges.get(0));
		}

		@Override
		protected Coefficients getCoefficients(List<Float> coefficients) {
			return new Coefficients(coefficients, indexFrom);
		}

		@Override
		protected Coefficients wrapCoefficients(Coefficients coefficients) {
			return coefficients;
		}
	};

	private ControlKey controlKey;

	public SimpleCoefficientParser1(Class<K1> keyClass, int indexFrom, ControlKey controlKey) {
		this.indexFrom = indexFrom;
		this.controlKey = controlKey;
	}

	@Override
	public Map<K1, Coefficients> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseException {
		var matrixMapResult = delegate.parse(is, control);
		return MatrixMap.cast(matrixMapResult);
	}

	public <K> BaseCoefficientParser<Coefficients, Coefficients, MatrixMap<Coefficients>>
			key(int length, String name, ValueParser<K> parser, Collection<K> range, String errorTemplate) {
		return delegate.key(length, name, parser, range, errorTemplate);
	}

	public BaseCoefficientParser<Coefficients, Coefficients, MatrixMap<Coefficients>> regionKey() {
		return delegate.regionKey();
	}

	public BaseCoefficientParser<Coefficients, Coefficients, MatrixMap<Coefficients>> ucIndexKey() {
		return delegate.ucIndexKey();
	}

	public BaseCoefficientParser<Coefficients, Coefficients, MatrixMap<Coefficients>> groupIndexKey(int maxGroups) {
		return delegate.groupIndexKey(maxGroups);
	}

	public BaseCoefficientParser<Coefficients, Coefficients, MatrixMap<Coefficients>> speciesKey(String name) {
		return delegate.speciesKey(name);
	}

	public BaseCoefficientParser<Coefficients, Coefficients, MatrixMap<Coefficients>> speciesKey() {
		return delegate.speciesKey();
	}

	public BaseCoefficientParser<Coefficients, Coefficients, MatrixMap<Coefficients>> space(int length) {
		return delegate.space(length);
	}

	public <K> BaseCoefficientParser<Coefficients, Coefficients, MatrixMap<Coefficients>>
			coefficients(int number, int length) {
		return delegate.coefficients(number, length);
	}

	@Override
	public ControlKey getControlKey() {
		return this.controlKey;
	}

}

package mmseg4j.solr;

import mmseg4j.analysis.CutLetterDigitFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.Map;

/**
 * CutLetterDigitFilter 支持在 solr 上配置用。
 *
 * @author chenlb 2010-12-17下午10:10:48
 */
public class CutLetterDigitFilterFactory extends TokenFilterFactory {

	public CutLetterDigitFilterFactory(Map<String, String> args) {
		super(args);
	}

	@Override
	public TokenStream create(TokenStream input) {

		return new CutLetterDigitFilter(input);
	}

}

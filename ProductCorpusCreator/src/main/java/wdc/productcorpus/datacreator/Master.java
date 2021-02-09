package wdc.productcorpus.datacreator;


import com.beust.jcommander.JCommander;

import wdc.productcorpus.datacreator.Extractor.IdentifierExtractor;
import wdc.productcorpus.datacreator.Extractor.OrderVerifier;
import wdc.productcorpus.datacreator.Filter.BadPLDsDetect;
import wdc.productcorpus.datacreator.Filter.ClassFilter;
import wdc.productcorpus.datacreator.Filter.FilterCombination;
import wdc.productcorpus.datacreator.Filter.RankingBasedNodeFilter;
import wdc.productcorpus.datacreator.Filter.TextualValueFilter;
import wdc.productcorpus.datacreator.Filter.ValuesListClassFilter;
import wdc.productcorpus.datacreator.Profiler.IDInfoProfiler;
import wdc.productcorpus.datacreator.Profiler.PropFrequencyPerValue;
import wdc.productcorpus.datacreator.Profiler.PropFrequencyPerValueJSON;
import wdc.productcorpus.util.Deduplicator;
import wdc.productcorpus.util.DeduplicatorSerial;
import wdc.productcorpus.util.NormalizationCorrection;
import wdc.productcorpus.util.Sampler;
import wdc.productcorpus.util.ValueSearch;

public class Master {

	public static void main(String[] args) {

		Master master = new Master();
		JCommander jc = new JCommander(master);

		IdentifierExtractor id_extract = new IdentifierExtractor();
		OrderVerifier order_verify = new OrderVerifier();
		ClassFilter filter_classes = new ClassFilter();
		TextualValueFilter filter_text = new TextualValueFilter();
		RankingBasedNodeFilter filter_nodes = new RankingBasedNodeFilter();
		IDInfoProfiler profile = new IDInfoProfiler();
		FilterCombination combine_filter = new FilterCombination();
		ValueSearch searchValue = new ValueSearch();
		ValuesListClassFilter filter_classes_Wvalues = new ValuesListClassFilter();
		Sampler sample = new Sampler();
		DeduplicatorSerial deduplicate = new DeduplicatorSerial();
		BadPLDsDetect filter_plds = new BadPLDsDetect();
		PropFrequencyPerValue profile_property = new PropFrequencyPerValue();
		PropFrequencyPerValueJSON profile_propertyJSON = new PropFrequencyPerValueJSON();
		
		jc.addCommand("extractNodesWithIDs", id_extract);
		jc.addCommand("verifyNoDuplicates", order_verify);
		jc.addCommand("filterClasses", filter_classes);
		jc.addCommand("filterTextualValues", filter_text);
		jc.addCommand("filterNode", filter_nodes);
		jc.addCommand("filterCombination", combine_filter);
		jc.addCommand("deduplicate", deduplicate);

		jc.addCommand("profile", profile);
		jc.addCommand("searchValue", searchValue);
		jc.addCommand("filterClassesWithValues", filter_classes_Wvalues);
		jc.addCommand("sample", sample);
		jc.addCommand("filterplds", filter_plds);
		jc.addCommand("profileProperty", profile_property);
		jc.addCommand("profilePropertyJSON", profile_propertyJSON);

		
		try {

			jc.parse(args);
			switch (jc.getParsedCommand()) {
			case "extractNodesWithIDs":
				id_extract.process();
				break;
			case "verifyNoDuplicates":
				order_verify.process();
				break;
			case "filterClasses":
				filter_classes.process();
				break;
			case "filterTextualValues":
				filter_text.process();
				break;
			case "filterNode":
				filter_nodes.process();
				break;
			case "filterCombination":
				combine_filter.process();
				break;
			case "filterplds":
				filter_plds.process();
				break;
			case "profile":
				profile.process();
				break;
			case "searchValue":
				searchValue.process();
				break;
			case "filterClassesWithValues":
				filter_classes_Wvalues.process();
				break;
			case "sample":
				sample.process();
				break;
			case "deduplicate":
				deduplicate.deduplicate();
				break;
			case "profileProperty":
				profile_property.process();
				break;
			case "profilePropertyJSON":
				profile_propertyJSON.process();
				break;
			
			}

		} catch (Exception pex) {
			if (jc.getParsedCommand() == null) {
				jc.usage();
			} else {
				switch (jc.getParsedCommand()) {
				case "extractNodesWithIDs":
					new JCommander(id_extract).usage();
					break;
				
				case "verifyNoDuplicates":
					new JCommander(order_verify).usage();
					break;					
				case "filterClasses":
					new JCommander(filter_classes).usage();
					break;
				case "filterTextualValues":
					new JCommander(filter_text).usage();
					break;
				case "filterNode":
					new JCommander(filter_nodes).usage();
					break;
				case "filterCombination":
					new JCommander(combine_filter).usage();
					break;
				case "filterplds":
					new JCommander(filter_plds).usage();
					break;
				case "profile":
					new JCommander(profile).usage();
					break;
				case "searchValue":
					new JCommander(searchValue).usage();
					break;
				case "filterClassesWithValues":
					new JCommander(filter_classes_Wvalues).usage();
					break;
				case "sample":
					new JCommander(sample).usage();
					break;
				case "deduplicate":
					new JCommander(deduplicate).usage();
					break;
				case "profileProperty":
					new JCommander(profile_property).usage();
					break;
				case "profilePropertyJSON":
					new JCommander(profile_propertyJSON).usage();
					break;
				
				default:
					jc.usage();
				}
			}
		}

	}

}

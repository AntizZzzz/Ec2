package com.jiangyifen.ec2.utils;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.wltea.analyzer.lucene.IKQueryParser;
import org.wltea.analyzer.lucene.IKSimilarity;

/**
 * 检索内容的类 
 * @author Administrator
 *
 */
public class SearchQueryOK {
	public static String INDEX_DIR = "F:\\lucencedir\\index"; // 索引存放目录
	public static int TOP_NUM = 30; // 显示前30 条结果

	/**
	 * 查询
	 * @param indexDir 检索的文件夹
	 * @param keyword 检索的关键字
	 * @return 返回搜索的结果字符串
	 */
	public static List<String> search(String indexDirStr, String keyword) throws Exception {
		File indexDir=new File(indexDirStr);
		//用来存储程序最后返回的所有信息
		List<String> resultMsg=new ArrayList<String>();
		
		//指定要检索的文件夹
		IndexSearcher is = new IndexSearcher(FSDirectory.open(indexDir), true); // read-only
		
		//要检索的字段
		String[] field = { "TTT", "modified", "filename" };
		
		//记录检索的开始时间
		long start = new Date().getTime(); // start time

		// 设定分词器
		Analyzer analyzer = new IKAnalyzer(); 
		
		//对多个字段进行解析
		Query query = IKQueryParser.parseMultiField(field, keyword);

		// 实例化搜索器
		IndexSearcher isearcher = new IndexSearcher(FSDirectory.open(indexDir));
		
		// 在索引器中使用IKSimilarity 相似度评估器
		isearcher.setSimilarity(new IKSimilarity());

		//设置排序字段
		Sort sort = new Sort(new SortField("path", SortField.DOC, false));
		
		// 搜索相似度最高的记录
		TopDocs topDocs = isearcher.search(query, null, TOP_NUM, sort);

		//文档的评分
		ScoreDoc[] hits = topDocs.scoreDocs;
		
		//设置Html样式
		SimpleHTMLFormatter simpleHtmlFormatter = new SimpleHTMLFormatter(
				"<span style='color:#ff0000'>", "</span>"); // 设定高亮显示的格式，也就是对高亮显示的词组加上前缀后缀
		
		//根据html样式对检索内容设置高亮显示
		Highlighter highlighter = new Highlighter(simpleHtmlFormatter,new QueryScorer(query));
		
		//对于检索出来的结果进行循环
				for (int i = 0; i < hits.length; i++) {
					Document doc = is.doc(hits[i].doc);
					String docTTT = doc.get("TTT");
//					// 想必大家在使用搜索引擎的时候也没有一并把全部数据展示出来吧，当然这里也是设定只展示部分数据
					highlighter.setTextFragmenter(new SimpleFragmenter(docTTT.length())); // 设置每次返回的字符数.
//					
//					//结果流1
					TokenStream tokenStream = analyzer.tokenStream("",new StringReader(docTTT));
					String str = highlighter.getBestFragment(tokenStream, docTTT);
					resultMsg.add(str);
		//
//					String docModified = doc.get("filename");
//					highlighter.setTextFragmenter(new SimpleFragmenter(docModified.length()));
		//
//					//将每个字段的返回结果放入到要返回的结果中
//					List<Fieldable> list = doc.getFields();
//					for (int j = 0; j < list.size(); j++) {
//						Fieldable fieldable = list.get(j);
//						resultMsg.add(fieldable.name() + " : "
//								+ fieldable.stringValue() + "<br>");
//						resultMsg.add(docTTT);
//					}
				}


		long end = new Date().getTime(); // end time

		resultMsg.add("为您找到相关结果 " + hits.length + " 个 (耗时 "
				+ (end - start) + " milliseconds)");
		return resultMsg;
	}

}
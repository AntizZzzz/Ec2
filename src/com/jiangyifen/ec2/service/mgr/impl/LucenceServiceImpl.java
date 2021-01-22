package com.jiangyifen.ec2.service.mgr.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
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

import com.jiangyifen.ec2.entity.Knowledge;
import com.jiangyifen.ec2.service.mgr.LucenceService;
import com.jiangyifen.ec2.utils.Config;

public class LucenceServiceImpl implements LucenceService{
	public static int TOP_NUM = 30; // 显示前15 条结果
	
	@Override
	public Boolean indexFile(String filePath, String toPath) {
		long start = new Date().getTime();
		
		//如果文件不存在则创建文件
		File indexFile=new File(toPath);
		File toFile=new File(filePath);
		
		if (!indexFile.exists()||!toFile.exists()) {
			if (!indexFile.getParentFile().exists()) {
				indexFile.getParentFile().mkdirs();
			}
			if (!toFile.getParentFile().exists()) {
				toFile.getParentFile().mkdirs();
			}
			try {
				indexFile.mkdir();
				toFile.mkdir();
			} catch (Exception e) {
				throw new RuntimeException("无法再指定位置创建新文件！");
			}
		}
		// 创建索引，并返回创建的索引的个数
		int numIndexed;
		try {
			numIndexed = index(indexFile, toFile);
		} catch (IOException e) {
			System.err.println("chb：Lucence 创建Index文件失败！！");
			e.printStackTrace();
			return false;
		}
																			// 方法
		long end = new Date().getTime();
		System.out.println("Indexing " + numIndexed + " files took "
				+ (end - start) + " milliseconds");
		return true;
	}

	/**
	 * 索引dataDir 下的.txt 文件，并储存在indexDir 下，返回索引的文件数量
	 * 
	 * @param indexDir
	 * @param dataDir
	 * @return int
	 * @throws IOException
	 */
	public static int index(File indexDir, File dataDir) throws IOException {

		if (!dataDir.exists() || !dataDir.isDirectory()) {
			throw new IOException(dataDir
					+ " does not exist or is not a directory");
		}
		// 使用IK分词器
		Analyzer analyzer = new IKAnalyzer(); // 采用的分词器

		// 第三个参数 为true 表示新建，false 表示添加到原有索引中
		IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir),
				analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

		// 对指定文件夹递归创建索引
		indexDirectory(writer, dataDir); // 调用indexDirectory 方法

		int numIndexed = writer.numDocs();
		writer.optimize();
		writer.close();
		return numIndexed;
	}

	/**
	 * 循环遍历目录下的所有.txt 文件并进行索引
	 * 
	 * @param writer
	 * @param dir
	 * @throws IOException
	 */
	private static void indexDirectory(IndexWriter writer, File dir)
			throws IOException {

		File[] files = dir.listFiles();

		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				indexDirectory(writer, f); // recurse
			} else if (f.getName().endsWith(".txt")) {
				indexFile(writer, f);
			}
		}
	}

	/**
	 * 对所有文件饿索引最终都会转移到对单个txt 文件进行索引
	 * 
	 * @param writer
	 * @param f
	 * @throws IOException
	 */
	private static void indexFile(IndexWriter writer, File f)
			throws IOException {
		// 如果是隐藏文件或者文件不存在，或者文件不可读，则直接返回
		if (f.isHidden() || !f.exists() || !f.canRead()) {
			return;
		}

		// 写出正在索引的文件的路径
		System.out.println("Indexing " + f.getCanonicalPath());

		Document doc = new Document();

		// 对文件名创建的索引
		doc.add(new Field("filename", f.getCanonicalPath(), Field.Store.YES,
				Field.Index.ANALYZED));

		// 对文件的内容创建索引
		String temp = fileReaderAll(f.getCanonicalPath(), "utf-8");
		doc.add(new Field("TTT", temp, Field.Store.YES, Field.Index.ANALYZED));

//		// 对文件的路径创建索引
//		doc.add(new Field("path", f.getPath(), Field.Store.YES,
//				Field.Index.ANALYZED));
//
//		// 对文件的修改时间创建索引
//		doc.add(new Field("modified", DateTools.timeToString(f.lastModified(),
//				DateTools.Resolution.MINUTE), Field.Store.YES,
//				Field.Index.ANALYZED));

		// 将文件读入内存
		FileInputStream fis = new FileInputStream(f);
		// 按照 UTF-8 编码方式将字节流转化为字符流
		InputStreamReader isr = new InputStreamReader(fis, "utf-8");
		// 从字符流中获取文本并进行缓冲
		BufferedReader br = new BufferedReader(isr);

		// 将文件写入到文档
		doc.add(new Field("contents", br));

		writer.setUseCompoundFile(false);
		writer.addDocument(doc);
	}

	/**
	 * 按照指定的字符编码从文件中读取文件的内容，然后拼装成一个字符串返回
	 * @param FileName
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static String fileReaderAll(String FileName, String charset)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(FileName), charset));
		String line = new String();
		String temp = new String();

		while ((line = reader.readLine()) != null) {
			temp += line;
		}
		reader.close();
		return temp;
	}

	
	/**
	 * 查询
	 * @param indexDir 检索的文件夹
	 * @param keyword 检索的关键字
	 * @return 返回搜索的结果字符串
	 */
	public List<String> search(String indexDirPath, String keyword) throws Exception {
		//用来存储程序最后返回的所有信息
		List<String> resultMsg=new ArrayList<String>();
		
		File indexDir=new File(indexDirPath);
		if(!indexDir.exists()){
			return resultMsg;
		}
		
		//指定要检索的文件夹
		IndexSearcher is = new IndexSearcher(FSDirectory.open(indexDir), true); // read-only
		
		//要检索的字段
		String[] field = { "TTT","filename" };
		
		//记录检索的开始时间
		long start = new Date().getTime(); // start time

		//对多个字段进行解析
		Query query = IKQueryParser.parseMultiField(field, keyword);

		// 设定分词器
		Analyzer analyzer = new IKAnalyzer();
				
		// 实例化搜索器
		IndexSearcher isearcher = new IndexSearcher(FSDirectory.open(indexDir));
		
		// 在索引器中使用IKSimilarity 相似度评估器
		isearcher.setSimilarity(new IKSimilarity());

		//设置排序字段
		Sort sort = new Sort(new SortField("filename", SortField.DOC, false));
		
		// 搜索相似度最高的记录
		TopDocs topDocs = isearcher.search(query, null, TOP_NUM, sort);

		//文档的评分
		ScoreDoc[] hits = topDocs.scoreDocs;
		
		//设置Html样式
		SimpleHTMLFormatter simpleHtmlFormatter = new SimpleHTMLFormatter(
				"<span style='color:#ff0000'>", "</span>"); // 设定高亮显示的格式，也就是对高亮显示的词组加上前缀后缀
		
		//根据html样式对检索内容设置高亮显示
		Highlighter highlighter = new Highlighter(simpleHtmlFormatter,
				new QueryScorer(query));
		// 对于检索出来的结果进行循环
		for (int i = 0; i < hits.length; i++) {
			Document doc = is.doc(hits[i].doc);
			String docTTT = doc.get("TTT");
			// //这里设定只展示部分数据
			highlighter
					.setTextFragmenter(new SimpleFragmenter(docTTT.length())); // 设置每次返回的字符数.

			// 结果流1
			TokenStream tokenStream = analyzer.tokenStream("",
					new StringReader(docTTT));
			String str = highlighter.getBestFragment(tokenStream, docTTT);
			resultMsg.add(str);
		}
		analyzer.close();
		is.close();
		isearcher.close();

		long end = new Date().getTime(); // end time

		resultMsg.add("为您找到相关结果 " + hits.length + " 个 (耗时 "
				+ (end - start) + " milliseconds)");
		return resultMsg;
	}

	/**
	 * 根据数据库中的知识生成不存在的文件
	 * 
	 */
	@Override
	public Boolean generateFile(Long domainId, List<Knowledge> knowledgeList,
			Boolean isDeleteOld) {
		String knowledgebase=(String)Config.props.get("knowledgebase");
		knowledgebase=knowledgebase+domainId+"/file/";
		
		for(Knowledge knowledge:knowledgeList){
			String title=knowledge.getTitle();
			String content=knowledge.getContent();
			// 把数据存到txt文件中
			File file = null;
			file = new File(knowledgebase+title+".txt");
			
			//文件存在并且不删除旧文件，继续下一个
			if(file.exists()&&!isDeleteOld){
				continue;
			}
			
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				try {
					file.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException("无法再指定位置创建新文件！");
				}
			}
//			else {
//				file.delete();
//			}
			
			//写文件
			try {
				BufferedWriter output = new BufferedWriter(new FileWriter(file));
				output.write(content);
				output.close();
			} catch (IOException e) {
				throw new RuntimeException("写入文件失败！");
			}	
		}
		return true;
	}
}

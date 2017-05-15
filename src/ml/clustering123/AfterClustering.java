package ml.clustering123;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;


public class AfterClustering {
	final static boolean isCooperative = true;
	final static String Replace ="LFU";
	
	static String NeighborFile = "SCNeighborPuvSplit20.txt";//0715.txt";//"SCNeighborTestingRandomSplit.txt";//"SCNeighborTestingRandomSplit.txt";//"SCNeighborCosNew.txt";//"SCNeighborTestingPuvSplit.txt";
	static String inputclusfile ="ClusterResultTrainingCos20.txt";//0630.txt";//"ClusterResultPuvTraining0630.txt";//"ClusterResultALLRandomTraining0715.txt"; //"ClusterResultRandomTestingSplit.txt";//"ClusterResultCosNew.txt";//"ClusterResultPuvTestingSplit.txt";//"ClusterResultPuvNew.txt";//"ClusterResultPuvNew.txt";  //ClusterResult2.txt
	static String requestfile = "ReadFileRequestTraining20.txt";//0715.txt";//"ReadFileRequestTesting.txt";//"ReadFileRequest.txt";//"ReadFileRequestTesting.txt";//"ReadFileRequest.txt";
	static int InitCacheSize =6;

	static int nClus = 13;//9 ; //=15;   // # Cluster
	
	static int FullVideoSize = 30;

	
	static int m_nEndUserSize = 214;//2385;//925;//1962;//1779;//1963;//1161;//2655;//1779;//722;//1161;//2655;//1161;
	static int m_nEndItemSize = 20;//30;//10;//11;
	static int m_nEndRequestSize = 14471;//3517;//2640;//1950;//3071;//2198;//2131;//3071;// 1623;//2306;//1623;//3071;//5414;//3071;
	static int[] ClusUcount;

	static double[] CachedVideoP;
	
	public static class Request{
		int ID=-1;
		int timestamp = -1;
		int UserID=-1;
		int VideoID=-1;
	}	
	public static class File{
		int ID = -1;
		int Fix_ID = -1;
		LinkedList<Integer> User = new LinkedList<Integer>();
		LinkedList<Integer> CachedSC = new LinkedList<Integer>();
		int count = 0;
		int count_duplicate = 0;		
	}	
	
	public static class EndUser{
		int ID = -1;
		LinkedList<Integer> ExistFile = new LinkedList<Integer>();
		int GroupID = -1;
	}	
	public static class SC{
		int ID = -1;
		LinkedList<Integer> TestUser = new LinkedList<Integer>();
		LinkedList<Integer> User = new LinkedList<Integer>();
		LinkedList<Integer> NeighborSC = new LinkedList<Integer>();	
				
		int[] CacheFile = new int [m_nEndItemSize];
		double[] CachedProportion = new double [m_nEndItemSize];
		int[] FileLastTime = new int [m_nEndItemSize];
		int[] ClickTime = new int [m_nEndItemSize];
		double remaining_space = InitCacheSize*FullVideoSize;
	}
	
	static File[] file;
	static Request[] request;

	static EndUser[] enduser;
	static SC[] sc;
	
	
	
	public static void main(String[] args) throws Exception{
		enduser = new EndUser[m_nEndUserSize];
		file = new File[m_nEndItemSize];
		request = new Request[m_nEndRequestSize];

		ReadRequestInfo(requestfile);
		
		for(int i=0;i<m_nEndUserSize; i++){
			enduser[i] = new EndUser();
			enduser[i].ID = i;
		}
		
		sc = new SC[nClus];
		for(int i=0;i<nClus; i++){
			sc[i] = new SC();
			sc[i].ID = i;
			
		}

			
	           
	        
		/////////////////  Read Spectral Clustering Result //////////////////////					
		ClusUcount = new int[nClus];
		ReadClusResult(inputclusfile); //ClusterResult.txt  //RandomResult.txt

		
		
		ServeRateCal_SC_edit.Cal(isCooperative, NeighborFile);

		for(int i=0; i<nClus; i++){
			for(int j=0; j<sc[i].CacheFile.length; j++){
				System.out.print(sc[i].CachedProportion[j]+"\t");
			}
			System.out.println();
		}
		
		
			 
			
	}
		
	private static void ReadClusResult(String Input) throws IOException{
		BufferedReader br;
		br = new BufferedReader(new FileReader(Input));
		
		String line;				
		while ((line = br.readLine()) != null) {     
			String items[] = line.split("\t");
			for(int i=1; i<items.length; i++){
				sc[Integer.valueOf(items[0])].User.offer(Integer.valueOf(items[i]));
				ClusUcount[Integer.valueOf(items[0])]++;
				enduser[Integer.valueOf(items[i])].GroupID=Integer.valueOf(items[0]);
			}
		}
		for(int i =0; i < nClus; i++){
			System.out.println("Cluster " +i + "# user = " + ClusUcount[i]);
			
		}
	}	

	private static void ReadRequestInfo(String Input) throws Exception, IOException{
		
		BufferedReader br;
		br = new BufferedReader(new FileReader(Input));
		
		String line;		

		for(int i=0;i<m_nEndUserSize; i++){
			enduser[i] = new EndUser();
			enduser[i].ID = i;
		}
		for(int i=0;i<m_nEndItemSize; i++){
			file[i] = new File();
			file[i].ID = i;
			file[i].Fix_ID = i;
		}		
		for(int i=0;i<m_nEndRequestSize; i++){
			request[i] = new Request();
		}	
		int rid=0;
		while ((line = br.readLine()) != null) {        	
        	String items[] = line.split("\t"); // #Timestamp uid fid
        	String timestamp=items[0];
        	int uid = Integer.valueOf(items[1]);
        	int fid = Integer.valueOf(items[2]);
        	enduser[uid].ExistFile.offer(fid);
        	file[fid].count++;
        	file[fid].User.offer(uid);
	        request[rid].ID=rid;
			request[rid].timestamp=Integer.valueOf(timestamp);	
			request[rid].UserID=uid;
			request[rid].VideoID=fid;
			rid++;
	        
		}   
		
		System.out.println("Request Num = " + rid);	
		
	}

}


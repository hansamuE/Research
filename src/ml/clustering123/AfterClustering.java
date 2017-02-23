package ml.clustering123;

import static ml.utils.Matlab.find;
import static ml.utils.Time.tic;
import static ml.utils.Time.toc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import la.matrix.Matrix;
import ml.clustering.Clustering;
import ml.options.ClusteringOptions;
import ml.options.SpectralClusteringOptions;


public class AfterClustering {
//	static String inputfile = "youtube.parsed.012908.dat"; //0311084hr//012908
	final static boolean isCooperative = true;
	final static String Replace ="LFU";
	
	static String NeighborFile = "SCNeighborPuvSplit20.txt";//0715.txt";//"SCNeighborTestingRandomSplit.txt";//"SCNeighborTestingRandomSplit.txt";//"SCNeighborCosNew.txt";//"SCNeighborTestingPuvSplit.txt";
	static String inputclusfile ="ClusterResultTrainingCos20.txt";//0630.txt";//"ClusterResultPuvTraining0630.txt";//"ClusterResultALLRandomTraining0715.txt"; //"ClusterResultRandomTestingSplit.txt";//"ClusterResultCosNew.txt";//"ClusterResultPuvTestingSplit.txt";//"ClusterResultPuvNew.txt";//"ClusterResultPuvNew.txt";  //ClusterResult2.txt
	static String requestfile = "ReadFileRequestTraining20.txt";//0715.txt";//"ReadFileRequestTesting.txt";//"ReadFileRequest.txt";//"ReadFileRequestTesting.txt";//"ReadFileRequest.txt";
	static String posfile = "user position.txt";
	static boolean isTimeWeight = true;
	static int thres_distance = 10000; //user's distance	
	static int InitCacheSize =6;
	static int thre_requestfreq = 86400; //3000
//	static double cluthres = 1.3;
	
//	static double average_neighbor = 0;
//	static int m_nUserSize = 16337;//9500;//16337;//540;//1835;//5047;
//	static int m_nItemSize = 303332;//110578;//303332;//2135;//8933;//35744;
//	static int m_nRequest = 611968;//200000;//611968;//2747;//12323;//54793;
//	static int m_nTestReqest=0;
//	static int timethres = 1902364085;//1202364085;
//	static int thres_usercount = 15;//15; //8,3,2
//	static int thres_filecount = 200;//200; //18,5,2	
	static int videolength = 172800;//3600; //660;//172800
	
	static int nClus = 13;//9 ; //=15;   // # Cluster
	
	static int FullVideoSize = 30;

	
//	static int num_served=0;
//	static int num_neighdown=0;
//	static int num_download = 0;
	static int m_nEndUserSize = 214;//2385;//925;//1962;//1779;//1963;//1161;//2655;//1779;//722;//1161;//2655;//1161;
	static int m_nEndItemSize = 20;//30;//10;//11;
	static int m_nEndRequestSize = 14471;//3517;//2640;//1950;//3071;//2198;//2131;//3071;// 1623;//2306;//1623;//3071;//5414;//3071;
//	static int ClusterUserSize;
	static int[] ClusUcount;
	static int[] ClusRcount;
	
	static double[][] ContentFrequency;
	static double[][] Puv;
	static double[][] Graph;
	static int[][] ClusterUser;
	
	static double[] CachedVideoP;
	
	public static class Request{
		int ID=-1;
		int timestamp = -1;
		int UserID=-1;
		int VideoID=-1;
	}	
//	public static class User{
//		int ID = -1;
//		int Fix_ID = -1;
//		//int requestuser = -1;
//		String Name;
//		LinkedList<Integer> File = new LinkedList<Integer>();
//		int count = 0;
//		int count_duplicate = 0;			
//	}	
	public static class File{
		int ID = -1;
		int Fix_ID = -1;
		String Name;
		LinkedList<Integer> User = new LinkedList<Integer>();
		LinkedList<Integer> CachedSC = new LinkedList<Integer>();
		int count = 0;
		int count_duplicate = 0;		
	}	
	
	public static class EndUser{
		int ID = -1;
		double posx = -1;
		double posy = -1;
		LinkedList<Integer> ExistFile = new LinkedList<Integer>();
		LinkedList<Integer> FileEndTime = new LinkedList<Integer>();
		LinkedList<Integer> Neighbor = new LinkedList<Integer>();
		int GroupID = -1;
	}	
	public static class SC{
		int ID = -1;
		double posx = -1;
		double posy = -1;
		int nRequest =0;
		LinkedList<Integer> TestUser = new LinkedList<Integer>();
		LinkedList<Integer> User = new LinkedList<Integer>();
		LinkedList<Integer> NeighborSC = new LinkedList<Integer>();	
				
//		LinkedList<Integer>	CachedFile = new LinkedList<Integer>();
//		LinkedList<Integer>	CachedProportion = new LinkedList<Integer>();  // 1/2 1/3 1/5
//		LinkedList<Integer>	FileLastTime = new LinkedList<Integer>();
		
		int[] CacheFile = new int [m_nEndItemSize];		
		double[] CachedProportion = new double [m_nEndItemSize];
		int[] FileLastTime = new int [m_nEndItemSize];
		int[] ClickTime = new int [m_nEndItemSize];
		double remaining_space = InitCacheSize*FullVideoSize;
	}
	
//	static User[] user;
	static File[] file;
	//static Request[] request;
//	static Request[] totalrequest;
	static Request[] request;
//	static Request[] testingrequest;
	
	
	static EndUser[] enduser;
	static SC[] sc;
	
	
	
	public static void main(String[] args) throws Exception{
//		user = new User[m_nUserSize];
		enduser = new EndUser[m_nEndUserSize];		
		file = new File[m_nEndItemSize];
		request = new Request[m_nEndRequestSize];
//		totalrequest = new Request[m_nRequest];
		
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
//			int[] ClusUcount = new int[nClus];
//			int[] ClusRcount = new int[nClus];
					
			
	           
	        
		/////////////////  Read Spectral Clustering Result //////////////////////					
		ClusUcount = new int[nClus];
		ReadClusResult(inputclusfile); //ClusterResult.txt  //RandomResult.txt
//		cal_fpopularity();
		
		
		
		ServeRateCal_SC_edit.Cal(isCooperative, NeighborFile);
//		ServeRateCal_SC.Cal(isCooperative, NeighborFile);

		for(int i=0; i<nClus; i++){
//			System.out.println("Clus "+i+": ");
			for(int j=0; j<sc[i].CacheFile.length; j++){
				System.out.print(sc[i].CachedProportion[j]+"\t");
			}
			System.out.println();
		}
		
		
			 
			
	}
		
		
//	}	
	

	
///////////////Function	
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
//		int client_num=0;
//		int video_num=0;		
//		int Client_ID=-1;
//		int Video_ID=-1;
//		int request_num =0;		
//		boolean find_the_same = false;
		
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
         	//String ClientIP = items[2];
         	//String VideoID = items[4];    
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

	private static void SetUserPosition(String Input) throws Exception, IOException{
		
		BufferedReader br;
		br = new BufferedReader(new FileReader(Input));
		String line;
		for(int i=0; i<enduser.length; i++){
			line = br.readLine();
			String items[] = line.split(" ");
			enduser[i].posx = Double.parseDouble(items[0]);
			enduser[i].posy = Double.parseDouble(items[1]);
			//System.out.print(enduser[i].posx+"\t"+enduser[i].posy+"\n");			
		}
		
}
	
	private static int[][] cal_fpopularity(){
		//int[] ClusRcount = new int[nClus];		
		int[][] fpopularity = new int [nClus][m_nEndItemSize];
		for(int rid =0; rid < m_nEndRequestSize; rid++){
			//ClusRcount[enduser[request[rid].UserID].GroupID]++;
			fpopularity[enduser[request[rid].UserID].GroupID][request[rid].VideoID]++;		
			//allfpopularity[ReadFile.file[i].Fix_ID] = ReadFile.file[i].count;
			//System.out.println(ReadFile.enduser[request[rid].UserID].GroupID);			
			
		}
		
		for(int i =0; i <nClus; i++){
			
			int fcount = 0;
			for(int j = 0; j < fpopularity[i].length; j++){
				fcount = fcount+ fpopularity[i][j];
				System.out.print(fpopularity[i][j]+"\t");
			}
			System.out.println();
			System.out.println("Clus " + (i+1) +" : " + fcount);
		}
		int fid=0; 
		for(int j=0; j<m_nEndItemSize; j++){
			for(int i=0; i<file.length; i++){			
				if(file[i].Fix_ID==j){
					System.out.println("Video "+fid + " : " + file[i].count_duplicate);
					fid++;
				}
			}
		}
		return fpopularity;		
	}
	
	

	
	
	private static double User_Distance(int ui, int uj){
		double answer=0;
		answer = Math.sqrt(Math.pow((enduser[ui].posx - enduser[uj].posx),2) + Math.pow((enduser[ui].posy - enduser[uj].posy),2));
		
		return answer;
	}
	

	private static void PrintMatrix(double[][] Matrix){
		java.text.DecimalFormat  df = new java.text.DecimalFormat("#.00");
		
		for(int i =0; i< Matrix.length; i++){
			for(int j=0;j<Matrix[0].length;j++){
				System.out.print(df.format(Matrix[i][j])+" ");
			}
			System.out.println();
		}
	}
	
	
}


package ml.clustering123;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;



public class RandomRequest {

	final static boolean isCooperative = false;
	final static String Replace ="LFU";
	
	static int nNei=1;
	
	static String NeighborFile = "SCNeighborPuvSplit46.txt";//0715.txt";//"SCNeighborTestingRandomSplit.txt";//"SCNeighborTestingRandomSplit.txt";//"SCNeighborCosNew.txt";//"SCNeighborTestingPuvSplit.txt";
//	static String inputclusfile ="ClusterResultALLRandomTraining0715.txt";//"ClusterResultPuvTraining0630.txt";//"ClusterResultALLRandomTraining0715.txt"; //"ClusterResultRandomTestingSplit.txt";//"ClusterResultCosNew.txt";//"ClusterResultPuvTestingSplit.txt";//"ClusterResultPuvNew.txt";//"ClusterResultPuvNew.txt";  //ClusterResult2.txt
	static String requestfile = "ReadFileRequestTesting46.txt";//0715.txt";//"ReadFileRequestTesting.txt";//"ReadFileRequest.txt";//"ReadFileRequestTesting.txt";//"ReadFileRequest.txt";
//	static String posfile = "user position.txt";
//	static boolean isTimeWeight = true;
//	static int thres_distance = 10000; //user's distance	
	static int InitCacheSize =1;
//	static int thre_requestfreq = 86400; //3000

//	static int videolength = 172800;//3600; //660;//172800
	
	static int nClus =9 ; //=15;   // # Cluster
	
	static int FullVideoSize = 30;


	static int m_nEndUserSize = 2385;//925;//1962;//1779;//1963;//1161;//2655;//1779;//722;//1161;//2655;//1161;
	static int m_nEndItemSize = 30;//10;//11;
	static int m_nEndRequestSize =3517;//2640;//1950;//3071;//2198;//2131;//3071;// 1623;//2306;//1623;//3071;//5414;//3071;
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

		
		int[] CacheFile = new int [m_nEndItemSize];		
		double[] CachedProportion = new double [m_nEndItemSize];
//		int[] FileLastTime = new int [m_nEndItemSize];
//		int[] ClickTime = new int [m_nEndItemSize];
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
		
		sc = new SC[nClus];
		for(int i=0;i<nClus; i++){
			sc[i] = new SC();
			sc[i].ID = i;
			
		}
		
		
		int[] FileLastTime = new int [m_nEndItemSize];
		int[] ClickTime = new int [m_nEndItemSize];
		
		ReadRequestInfo(requestfile);
		
		if(isCooperative){
			nNei=3;
			BufferedReader br;
			br = new BufferedReader(new FileReader(NeighborFile));
			String line;				
			while ((line = br.readLine()) != null) {     
				String items[] = line.split("\t");
				for(int i=1; i<items.length; i++){
					sc[Integer.valueOf(items[0])].NeighborSC.offer(Integer.valueOf(items[i]));					
				}
			}
		}				
		double[] served_amount = new double[nClus];		
		
		
//		double[] CachedVideoP= new double[m_nEndItemSize];
		CachedVideoP= new double[m_nEndItemSize];
		int Full=FullVideoSize;
		int nServed = 0;	
		int nDownload = 0;			
		double filesize = Full/nNei;
		for(int rid =0; rid < m_nEndRequestSize; rid++){				
			int uid = request[rid].UserID;
			int fid = request[rid].VideoID;
//			int gid = enduser[uid].GroupID;
			if(CachedVideoP[fid] >= Full){
				nServed++;
				FileLastTime[fid] = request[rid].timestamp;
				ClickTime[fid]++;
				Object[] array = file[fid].CachedSC.toArray();
				for(int i=0; i< array.length; i++){
					int index = (int) array[i];					
					served_amount[index] = served_amount[index] + filesize;
				}				
				//isDownload = false;
			}
			else if(CachedVideoP[fid] < Full){
				nDownload++;
				while(true){
					boolean isReplace = true;
					int gid =-1;
					double largestsize=0;
					for(int i=0; i< nClus; i++){						
						if(sc[i].remaining_space>=filesize && sc[i].remaining_space > largestsize){
							largestsize = sc[i].remaining_space;
							gid = i;
							isReplace = false;									
						}
					}
					
					
					if(!isReplace){
						//Download					
						sc[gid].remaining_space = sc[gid].remaining_space - filesize;
						file[fid].CachedSC.offer(gid);
						Object[] array = sc[gid].NeighborSC.toArray();					
						for(int i=0; i< array.length; i++){
							int index = (int) array[i];
							sc[index].remaining_space = sc[index].remaining_space - filesize;
							file[fid].CachedSC.offer(index);
						}
						FileLastTime[fid] = request[rid].timestamp;
						ClickTime[fid]++;
						CachedVideoP[fid]=Full;
						break;
					}
					
					else if (isReplace){
						int replaceid = -1;
						int clicktime = 99999999;
						for(int i=0; i<m_nEndItemSize; i++){
							if(!file[i].CachedSC.isEmpty()){
								if(ClickTime[i] < clicktime){
									replaceid = i;
									clicktime = ClickTime[replaceid];
								}
							}
						}
						System.out.println("replace: "+ replaceid);
						System.out.println("click time = " + clicktime);
						Object[] array = file[replaceid].CachedSC.toArray();
						for(int i=0; i< array.length; i++){
							int index = (int) array[i];
							sc[index].remaining_space = sc[index].remaining_space + filesize;
						}
						file[replaceid].CachedSC.clear();
						CachedVideoP[replaceid]=0;					
					}
				}
				
			}
				
		}		
		System.out.println("Served : " + nServed);
		System.out.println("Download : " + nDownload);	
		
		for(int i=0; i< nClus; i++){
//			System.out.println("Clus\t"+ i+"\tServed amount=\t"+ served_amount[i]/Full);
			System.out.println( served_amount[i]/Full);
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
	
}
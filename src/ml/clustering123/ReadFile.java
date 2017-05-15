package ml.clustering123;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;


public class ReadFile {
	static String inputfile = "history-input_10_196_5676.txt";//"youtube.parsed.012908.dat"; //0311084hr//012908
	static String OutputTrainingFile = "ReadFileRequestTraining10.txt";//0717.txt";
	static String OutputTestingFile = "ReadFileRequestTesting10.txt";//0717.txt";
	static String OutputPopularityFile = "PopularityTrainingCos10.txt";//0715.txt";
	static String ClusterOutputFile = "ClusterResultTrainingCos10.txt";//0715.txt";
	static String posfile = "user position.txt";
	static boolean isTimeWeight = false;
	static int thres_distance = 10000; //user's distance	
	static int thre_requestfreq = 86400; //3000
	static double cluthres = 1.3;
	
	static int m_nUserSize = 196;//16337;//9500;//16337;//540;//1835;//5047;
	static int m_nItemSize = 10;//303332;//110578;//303332;//2135;//8933;//35744;
	static int m_nRequest = 5676;//611968;//200000;//611968;//2747;//12323;//54793;
	static int timethres = 999999999;//2244475;//1902364085;//1202364085;
	static int thres_usercount = 0;//10;//10;//15; //8,3,2
	static int thres_filecount = 0;//150;//150;//200; //18,5,2
	static int thres_filecount_30 = 0;//150;//65;
	static int videolength = 86400;//3600; //660;//172800
	
	static int nClus =10 ; //=15;   // # Cluster
	static int Ori_nClus = 10;	

	static int m_nEndUserSize;
	static int m_nEndItemSize;
	static int m_nEndRequestSize;
	static int ClusterUserSize;
	static int[] ClusUcount= new int[nClus];

	static double[][] ContentFrequency;
	static double[][] Puv;
	static double[][] Graph;

	public static class Request{
		int ID=-1;
		int timestamp = -1;
		int UserID=-1;
		int VideoID=-1;
	}
	
	
	public static class User{
		int ID = -1;
		int Fix_ID = -1;
		String Name;
		LinkedList<Integer> File = new LinkedList<Integer>();
		int count = 0;

	}
	
	public static class File{
		int ID = -1;
		int Fix_ID = -1;
		String Name;
		LinkedList<Integer> User = new LinkedList<Integer>();
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
		LinkedList<Integer> User = new LinkedList<Integer>();
	}
	
	static User[] user;
	static File[] file;
	static Request[] totalrequest;
	static Request[] request;

	static EndUser[] enduser;
	static SC[] sc;
	
	
	
	public static void main(String[] args) throws Exception{
		user = new User[m_nUserSize];
		file = new File[m_nItemSize];
		totalrequest = new Request[m_nRequest];
		
		
		ReadRequestInfo(inputfile);
		CalculateNumUser();
		
		
		enduser = new EndUser[m_nEndUserSize];		
		for(int i=0;i<m_nEndUserSize; i++){
			enduser[i] = new EndUser();
			enduser[i].ID = i;
		}
		
		
		
		//ReadUserPostion
		if(thres_distance < 200){
			SetUserPosition(posfile);		
		}
		
		
		ContentFrequency = new double[m_nEndUserSize][m_nEndItemSize];
		Graph = new double[m_nEndUserSize][m_nEndUserSize];
		Puv = new double[m_nEndUserSize][m_nEndUserSize];
		
		
		GraphGeneration(); 
		FindRequest();
		
		
		sc = new SC[nClus];
		for(int i=0;i<nClus; i++){
			sc[i] = new SC();
			sc[i].ID = i;			
		}						
	           
	        
		/////////////////  Spectral Clustering  //////////////////////
			ClusterUserSize = m_nEndUserSize; 			
			double[][] CosMatrix = new double[m_nEndUserSize][m_nEndUserSize];
			for(int i=0; i< m_nEndUserSize-1; i++){
				for(int j=i+1; j< m_nEndUserSize; j++){
					CosMatrix[i][j]=GetCosSim(ContentFrequency[i],ContentFrequency[j]);
					CosMatrix[j][i]=CosMatrix[i][j];
				}				
			}
			
			Matrix Clustering_Result = SPECTRALClustering(Puv,nClus);
			int[] col = find(Clustering_Result).cols;
			int[] row = find(Clustering_Result).rows;	
			
			for(int i=0; i< col.length; i++){
				enduser[row[i]].GroupID=col[i];
				sc[col[i]].User.offer(row[i]);
				ClusUcount[col[i]]++;						
			}
        //////////////////////////////////////////////////////////////////			
			
			for(int i =0; i < nClus; i++){
				System.out.println("Cluster " +i + "# user = " + ClusUcount[i]);				
			}
			
			Modularity_Calculating();		
			ClusAgain();
		
			// Calculate to decide an optimal number of cluster
			FileWriter fw = new FileWriter(ClusterOutputFile);  
			BufferedWriter bufw = new BufferedWriter(fw);  
			for(int i=0; i<nClus; i++){
				Object[] array = sc[i].User.toArray();
				bufw.write(i+"\t");
				for(int j=0; j<array.length; j++){
					bufw.write(array[j]+"\t");
				}
				bufw.write("\n");
			}
			bufw.close();
			cal_fpopularity();
			Modularity_Calculating();			
	}
		
	private static void Modularity_Calculating(){
		System.out.println("nClus = " + nClus);
		double two_m = 0;
		for(int i = 0; i < enduser.length; i++){
			two_m = two_m + enduser[i].Neighbor.size();
		}
		double value_Q = 0;
		for(int i = 0; i < nClus; i++ ){
			Object[] Uarray = sc[i].User.toArray();
			
			for(int j = 0; j < Uarray.length; j++ ){
				for(int k = 0; k < Uarray.length; k++ ){
					if(j!=k){
						int ui = (int) Uarray[j];
						int uj = (int) Uarray[k];
						value_Q = value_Q + Graph[ui][uj]-((enduser[ui].Neighbor.size()*enduser[ui].Neighbor.size())/two_m);
					}
				}
			}
			
		}
		
		value_Q = value_Q/two_m;
		System.out.println("value_Q = " + value_Q);
	}
	
	private static void CalculateNumUser(){
		int requestfile=-1;

     	int fid=0;
     	int fid_30 = 10;  //11~30�W(10-29)
     	
     	int uid=0;
     	for(int r=0; r<totalrequest.length; r++){
     		if(totalrequest[r].timestamp < timethres){
     			int i = totalrequest[r].UserID;
     			int j = totalrequest[r].VideoID;
     			if(user[i].count>=thres_usercount){  	    			
     				requestfile=j;
	    				if (file[requestfile].count>thres_filecount){//18,5,2	    					
	    					if(file[requestfile].Fix_ID== -1){
	    						file[requestfile].Fix_ID=fid;
	    						fid++;
	    					}
	    					if(user[i].Fix_ID == -1){
	    						user[i].Fix_ID=uid;
	    						uid++;
	    					}
	    				}
	    				else if (file[requestfile].count < thres_filecount && file[requestfile].count>thres_filecount_30){
	    					if(file[requestfile].Fix_ID== -1){
	    						file[requestfile].Fix_ID=fid_30;
	    						fid_30++;
	    					}
	    				}
	    				
//	    			}	    			
     			}
     		}
    	}
    	System.out.println("user: "+uid+"\t"+"video: "+fid+"\t"+"video_30: "+fid_30);
    	m_nEndUserSize=uid;
    	m_nEndItemSize=fid;
    	int rid=0;
    	for(int i =0; i < m_nRequest; i++){
    		if(user[totalrequest[i].UserID].Fix_ID != -1 && file[totalrequest[i].VideoID].Fix_ID != -1 && totalrequest[i].timestamp < timethres){
    			rid++;    			
    		}
    	}
    	m_nEndRequestSize=rid;
    	System.out.println("Request: "+rid);
	}
	

	private static void ReadRequestInfo(String Input) throws Exception, IOException{
		
		BufferedReader br;
		br = new BufferedReader(new FileReader(Input));
		
		String line;		
		int client_num=0;
		int video_num=0;		
		int Client_ID=-1;
		int Video_ID=-1;
		int request_num =0;		
		boolean find_the_same = false;

		for(int i=0;i<m_nUserSize; i++){
			user[i] = new User();
		}		
		for(int i=0;i<m_nItemSize; i++){
			file[i] = new File();
		}		
		for(int i=0;i<m_nRequest; i++){
			totalrequest[i] = new Request();
		}	
		
		while ((line = br.readLine()) != null) {        	
        	String items[] = line.split("\t"); // #Timestamp  #YouTube server IP #Client IP    #Request #Video ID   #Content server IP
        	String timestamp=(String) items[0].subSequence(3,10);
        	for (int i = 0; i < user.length; i++){
        		if (user[i].ID == -1) break;	         		
	         	if(items[1].equals(user[i].Name)){
	         		find_the_same = true;
	         		Client_ID=user[i].ID;
	         		break;
	         	}	         		
	         }
	         if (find_the_same == false) {
         		user[client_num].ID=client_num;
         		user[client_num].Name = items[1];
         		user[client_num].count++;         			
         		Client_ID=user[client_num].ID;
         		client_num++;         			
         	}
	         find_the_same=false;
	         
	         for ( int i = 0; i < file.length; i++ ){
	         	if (file[i].ID == -1) break;	         		
	         	if(items[2].equals(file[i].Name)) {
	         		find_the_same = true;

	         		Video_ID=file[i].ID;
	         		
	         		if(Integer.valueOf(timestamp) <= timethres){
	         			file[Video_ID].count_duplicate++;
	         		}
	         		
	         		
	         		boolean duplicate=false;
	         		for(int request=0; request<totalrequest.length; request++){
	         			if (totalrequest[request].UserID == Client_ID 
	         					&& totalrequest[request].VideoID == Video_ID
	         					&& Integer.valueOf(timestamp) - totalrequest[request].timestamp < thre_requestfreq){
	         				duplicate=true;
	         				break;
	         				
	         			}
	         		}         		
	         		if(duplicate == false){
	         			if(Integer.valueOf(timestamp) <= timethres){
	         				file[Video_ID].count++;
	         			}
	         			user[Client_ID].count++;
	         			file[Video_ID].User.offer(Client_ID);
	             		user[Client_ID].File.offer(Video_ID);
	         		}
	         		
	         		break;
	         	}	         		
	         }
	         if (find_the_same == false) {
	         	file[video_num].ID=video_num;
         		file[video_num].Name = items[2];
         		Video_ID=file[video_num].ID;    
         		
         		if(Integer.valueOf(timestamp) <= timethres){
         			file[Video_ID].count++;
         			file[Video_ID].count_duplicate++;
         		}
         		
     			file[Video_ID].User.offer(Client_ID);
         		user[Client_ID].File.offer(Video_ID);
         		video_num++;
         		
	         }
	        find_the_same=false;
	        
	    			
	    	totalrequest[request_num].ID=request_num;
			totalrequest[request_num].timestamp=Integer.valueOf(timestamp);	
			totalrequest[request_num].UserID=Client_ID;
			totalrequest[request_num].VideoID=Video_ID;
			request_num++;
	        
		}   
		
		System.out.println("Request Num = " + request_num);
		System.out.println("Client num = " + client_num);
     	System.out.println("Video num = " + video_num);
		
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
		}
		
}
	
	private static int[][] cal_fpopularity() throws IOException{
		int[][] fpopularity = new int [nClus][m_nEndItemSize];
		for(int rid =0; rid < m_nEndRequestSize; rid++){
			fpopularity[enduser[request[rid].UserID].GroupID][request[rid].VideoID]++;

		}
		FileWriter fw = new FileWriter(OutputPopularityFile);
		BufferedWriter bufw = new BufferedWriter(fw);  
		for(int i =0; i <nClus; i++){
			
			int fcount = 0;
			for(int j = 0; j < fpopularity[i].length; j++){
				fcount = fcount+ fpopularity[i][j];
				System.out.print(fpopularity[i][j]+"\t");
				bufw.write(fpopularity[i][j]+"\t");
			}
			bufw.write("\n");
			System.out.println();
			System.out.println("Clus " + (i+1) +" : " + fcount);
		}
		bufw.close();
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
	
	
	private static boolean ClusAgain() throws IOException{
		boolean isClusAgain = false;
		int[][] fpopularity = new int [nClus][m_nEndItemSize];
		fpopularity = cal_fpopularity();

		int nc = nClus;
		for(int i =0; i < nc; i++){
				
			int fcount = 0;
			for(int j = 0; j < fpopularity[i].length; j++){
				fcount = fcount+ fpopularity[i][j];
				System.out.print(fpopularity[i][j]+"\t");
			}
			System.out.println();
			System.out.println("Clus " + (i+1) +" : " + fcount);	
			if(fcount > cluthres*m_nEndRequestSize / Ori_nClus){
				isClusAgain = true;
				System.out.println("Cluster again");
				Object[] SCuser = sc[i].User.toArray();
						
				sc[i].User.clear();
				ClusUcount[i]=0;
				double newPuv[][] = new double[SCuser.length][SCuser.length];
				for(int ua=0; ua< SCuser.length-1; ua++){
					for(int ub=ua+1; ub< SCuser.length; ub++){
						newPuv[ua][ub] = Puv[(int) SCuser[ua]][(int) SCuser[ub]];
						newPuv[ub][ua] = newPuv[ua][ub];
					}
				}
			
				//cluster again
				ClusterUserSize = SCuser.length;
				Matrix Clustering_Result = SPECTRALClustering(newPuv,2);
				
				nClus=nClus+1;				
				SC[] newsc = new SC [nClus];
				int[] newClusUcount = new int[nClus];
				
				for(int nclus=0; nclus<nClus-1; nclus++){
					newsc[nclus] = new SC();
					newsc[nclus] = sc[nclus];
					newClusUcount[nclus] = ClusUcount[nclus];
				}
				newsc[nClus-1] = new SC();
				
				
				int[] col = find(Clustering_Result).cols;
				int[] row = find(Clustering_Result).rows;		
				
				
				
				
				for(int c=0; c< col.length; c++){
					if(col[c]==0){
						enduser[(int) SCuser[row[c]]].GroupID = i;
						newsc[i].User.offer((int) SCuser[row[c]]);
						newClusUcount[i]++;
					}
					else if (col[c]==1){
						enduser[(int) SCuser[row[c]]].GroupID = nClus-1;
						newsc[nClus-1].User.offer((int) SCuser[row[c]]);
						newClusUcount[nClus-1]++;
					}		
				
				}
				
				sc = new SC [nClus];
				ClusUcount = new int[nClus];
				
				for(int nclus=0; nclus<nClus; nclus++){
					sc[nclus] = new SC();
					sc[nclus] = newsc[nclus];
					ClusUcount[nclus]=newClusUcount[nclus];
				}
			}
					
							

		}
				
		System.out.format("Elapsed time: %.3f seconds\n", toc());
		for(int i =0; i < nClus; i++){
			System.out.println("Cluster " +i + "# user = " + ClusUcount[i]);
		}
				
		return isClusAgain;
		
	}
	
	private static Matrix SPECTRALClustering(double[][] inputdata, int nC){
		tic();				
		boolean verbose = false;
		int maxIter = 50;
		String graphType = "nn";
		double graphParam = 6;
		String graphDistanceFunction = "euclidean";
		String graphWeightType = "distance";
		double graphWeightParam = 1;
		ClusteringOptions options = new SpectralClusteringOptions(
				nC,
				verbose,
				maxIter,
				graphType,
				graphParam,
				graphDistanceFunction,
				graphWeightType,
				graphWeightParam);
		
		Clustering spectralClustering = new SpectralClustering(options);
		
		double[][] data = inputdata;

		spectralClustering.feedData(data);
		spectralClustering.clustering(null);		
		Matrix Clustering_Result = spectralClustering.getIndicatorMatrix();
		return Clustering_Result;
	}

	private static void GraphGeneration(){
		System.out.println("GraphGeneration Start");
		
    	double[][] ContentRequestTime = new double[m_nEndUserSize][m_nEndItemSize];
    	double[][] TimeWeightMatrix = new double[m_nEndUserSize][m_nEndUserSize];
		
    	
    	for(int i=0; i<totalrequest.length; i++){
    		if(user[totalrequest[i].UserID].Fix_ID != -1 && file[totalrequest[i].VideoID].Fix_ID != -1){
    			
    			ContentFrequency[user[totalrequest[i].UserID].Fix_ID][file[totalrequest[i].VideoID].Fix_ID]++;
    			for(int j = 0; j < m_nEndUserSize; j++){
    				if(user[totalrequest[i].UserID].Fix_ID != j && ContentRequestTime[j][file[totalrequest[i].VideoID].Fix_ID]!=0){
    					TimeWeightMatrix[j][user[totalrequest[i].UserID].Fix_ID] += videolength/Math.abs(ContentRequestTime[j][file[totalrequest[i].VideoID].Fix_ID] - totalrequest[i].timestamp);
    					TimeWeightMatrix[user[totalrequest[i].UserID].Fix_ID][j] = TimeWeightMatrix[j][user[totalrequest[i].UserID].Fix_ID];
    				}
    			}
    			ContentRequestTime[user[totalrequest[i].UserID].Fix_ID][file[totalrequest[i].VideoID].Fix_ID] = totalrequest[i].timestamp;
    		}
    	}   	
    	
     	
     	for(int i=0; i<m_nEndUserSize-1; i++){
			for(int j=i; j<m_nEndUserSize;j++){
				if(i!=j){					
					if(isTimeWeight){
						Puv[i][j] = GetPuv(ContentFrequency[i], ContentFrequency[j], TimeWeightMatrix[i][j]);
					}
					else{
						Puv[i][j] = GetPuv(ContentFrequency[i], ContentFrequency[j], 1);
					}
					Puv[j][i] = Puv[i][j];
				}
			}
		}
     	for(int i=0; i<m_nEndUserSize-1; i++){
     		for(int j=i; j<m_nEndUserSize;j++){
     			if(Puv[i][j]==0 || User_Distance(i,j)>=thres_distance)
     				Graph[i][j]=0;
     			else{
     				double random_num= Math.random();
     				if(random_num>Puv[i][j]){
     					Graph[i][j]=0;
     					Graph[j][i]=0;

     				}
     				else{
     					
     					Graph[i][j]=1;
     					Graph[j][i]=1;
     					enduser[i].Neighbor.offer(j);
     					enduser[j].Neighbor.offer(i);
     				}
     			}
     		}
     	}   	
     	System.out.println("GraphGeneration End");
	}
	
	private static void FindRequest() throws IOException{
		request = new Request[m_nEndRequestSize];
		
		for(int i=0;i<m_nEndRequestSize; i++){
			request[i] = new Request();
			request[i].ID = i;
		}
		FileWriter fw = new FileWriter(OutputTrainingFile);  
		FileWriter testfw = new FileWriter(OutputTestingFile);
        BufferedWriter bufw = new BufferedWriter(fw);  
        BufferedWriter buftestw = new BufferedWriter(testfw);
		int rid = 0;
		int uid = m_nEndUserSize;
		for(int i=0;i<m_nRequest; i++){
			if(user[totalrequest[i].UserID].Fix_ID == -1 && file[totalrequest[i].VideoID].Fix_ID != -1){
				if(totalrequest[i].timestamp > timethres){
					user[totalrequest[i].UserID].Fix_ID =uid;
					uid++;
				}
			}
			if(user[totalrequest[i].UserID].Fix_ID != -1 && file[totalrequest[i].VideoID].Fix_ID != -1){
				if(totalrequest[i].timestamp > timethres){
					buftestw.write(totalrequest[i].timestamp+"\t"+user[totalrequest[i].UserID].Fix_ID+"\t"+file[totalrequest[i].VideoID].Fix_ID+"\n");
					trid++;
					continue;
				}
				else{
					request[rid].timestamp = totalrequest[i].timestamp;
					request[rid].UserID = user[totalrequest[i].UserID].Fix_ID;
					request[rid].VideoID = file[totalrequest[i].VideoID].Fix_ID;
					bufw.write(request[rid].timestamp+"\t"+request[rid].UserID+"\t"+request[rid].VideoID+"\n");
					rid++;
				}
				
			}
			
		}
		System.out.println("rid = "+rid);
		System.out.println("new user = " + uid);
 		bufw.close();
 		buftestw.close();
 	}
	
	private static double User_Distance(int ui, int uj){
		double answer=0;
		answer = Math.sqrt(Math.pow((enduser[ui].posx - enduser[uj].posx),2) + Math.pow((enduser[ui].posy - enduser[uj].posy),2));
		
		return answer;
	}
	
	
	private static double GetPuv(double[] ni, double[] nj, double Timeweight){
		double SimResult = 0.0;
		double Numerator = 0.0;
		
		for(int i=0; i<m_nEndItemSize; i++){
			if(Timeweight == 0){
				Numerator = Numerator + ( - ni[i] * nj[i]);	
			}
			else{
				Numerator = Numerator + ( - ni[i] * nj[i] *  Timeweight);		
			}
		}
		Numerator = Math.exp(Numerator);
		
		SimResult = 1-Numerator;
		return SimResult;		
	}
	
	private static double GetCosSim(double[] ni, double[] nj){
		double SimResult = 0.0;
		double Denominator_i = 0.0; //����
		double Denominator_j = 0.0; //����
		double Numerator = 0.0;   //���l
		
		for(int i=0; i<ni.length; i++){
			Numerator = Numerator + ( ni[i] * nj[i] );
			Denominator_i = Denominator_i + ni[i] * ni[i];
			Denominator_j = Denominator_j + nj[i] * nj[i];
		}
		SimResult = Numerator/(Math.sqrt(Denominator_i) * Math.sqrt(Denominator_j));
		return SimResult;	
	}
	
	
}


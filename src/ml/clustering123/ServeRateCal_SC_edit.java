package ml.clustering123;
import java.io.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServeRateCal_SC_edit{
	static double CachedVideoP=0;
	public static void Cal(boolean isCooperative, String NeighborFile) throws NumberFormatException, IOException{
		String popularityinput = "PopularityTrainingCos10.txt";//0715.txt";
		String type = "puv";
		double[][] ClusSim = new double[AfterClustering.nClus][AfterClustering.nClus];

		double[][] fpopularity = new double [AfterClustering.nClus][AfterClustering.m_nEndItemSize];
		double[][] ClusterFrequency =new double[AfterClustering.nClus][AfterClustering.m_nEndItemSize];
		double[][] TestingClusterFrequency =new double[AfterClustering.nClus][AfterClustering.m_nEndItemSize];
		int[] allfpopularity = new int [AfterClustering.m_nEndItemSize];
		
		fpopularity = ReadPopularityMatrix(popularityinput);
		for(int i=0; i<AfterClustering.file.length; i++){
			if(AfterClustering.file[i].Fix_ID!=-1){
				allfpopularity[AfterClustering.file[i].Fix_ID] = AfterClustering.file[i].count_duplicate;
			}
		}
		
		for(int i =0; i < AfterClustering.nClus; i++){
			double sum=0;
			for(int j = 0; j < fpopularity[i].length; j++){
				sum=sum+fpopularity[i][j];
				
			}
			for(int j = 0; j < fpopularity[i].length; j++){
				fpopularity[i][j] = fpopularity[i][j] / sum;  //Normalize
			}
		}
		for(int i =0; i < AfterClustering.nClus-1; i++){
			for(int j =i+1; j < AfterClustering.nClus; j++){
				int fi=0;
				int fj=0;				
				ClusSim[i][j] = GetPuv(fpopularity[i],fpopularity[j],0);
				System.out.print(ClusSim[i][j]+"\t");
				ClusSim[j][i] = ClusSim[i][j];
			}
			System.out.println();
		}
		

		if(isCooperative){
			BufferedReader br;
			br = new BufferedReader(new FileReader(NeighborFile));
			String line;				
			while ((line = br.readLine()) != null) {     
				String items[] = line.split("\t");
				for(int i=1; i<items.length; i++){
					AfterClustering.sc[Integer.valueOf(items[0])].NeighborSC.offer(Integer.valueOf(items[i])); 				
				}
			}
		}
		
		
		
		for(int i=0; i<AfterClustering.nClus; i++){
			System.out.print("Clus" +i+" : ");
			Object[] array = AfterClustering.sc[i].NeighborSC.toArray();
			for(int j=0; j<array.length; j++){
				System.out.print(array[j]+"\t");
			}
			System.out.println();			
		}
		
	
		ClusterFrequency = ReadPopularityMatrix(popularityinput);
		
		
		double[] served_amount = new double[AfterClustering.nClus];
		int Full=30;
		int nSelfServed = 0;
		int nNeighborServed = 0;
		int nDownload = 0;

		Timestamp timestamp_end = Timestamp.valueOf("2016-06-23 08:00:00");
		int request_no = 0;
		int download_no = 0;
		FileWriter fileWriter = new FileWriter("download_rate");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		for(int rid =0; rid < AfterClustering.m_nEndRequestSize; rid++){	
			CachedVideoP = 0;
			boolean isSelfServed = false;
			boolean isDownload = true;
			int uid = AfterClustering.request[rid].UserID;
			int fid = AfterClustering.request[rid].VideoID;
			int gid = AfterClustering.enduser[uid].GroupID;


			if (AfterClustering.request[rid].timestamp > Integer.valueOf((Long.toString(timestamp_end.getTime()).subSequence(3, 10)).toString())) {
				bufferedWriter.write(new Date(timestamp_end.getTime()) + "\t" + (double) download_no / request_no + "\n");
				timestamp_end.setTime(timestamp_end.getTime() + 86400000);
				request_no = 0;
				download_no = 0;
			}
			request_no++;
			
			if(gid == -1){
				AfterClustering.enduser[uid].ExistFile.offer(fid);
				double[] simmatrix = new double[AfterClustering.m_nEndItemSize];
				Object[] filelist=AfterClustering.enduser[uid].ExistFile.toArray();
				for(int i=0; i<filelist.length; i++){
					simmatrix[(int)filelist[i]]++;
				}
				double maxvalue=0;
				int maxclus =-1;
				for(int j=0; j<AfterClustering.nClus; j++){				
					double sim=0;
					if(type == "puv"){
						sim = GetPuv(simmatrix,fpopularity[j],0);
					}
					else if(type == "cos"){
						for(int k=0; k<AfterClustering.m_nEndItemSize; k++){
							sim=sim + simmatrix[k]*ClusterFrequency[j][k];		
						}
					}
								
					if(sim > maxvalue*1.3){
						maxvalue = sim;
						maxclus =j;
					}
				}
				gid=maxclus;
				

			}
			
			
			
			

			if(gid != -1){
				if(!AfterClustering.sc[gid].TestUser.contains(uid)){
					AfterClustering.sc[gid].TestUser.offer(uid);
				}
				TestingClusterFrequency[gid][fid]++;
				CachedVideoP = CachedVideoP +AfterClustering.sc[gid].CachedProportion[fid];
				Object[] neigh = AfterClustering.sc[gid].NeighborSC.toArray();
				for(int n=0; n<AfterClustering.sc[gid].NeighborSC.size(); n++){
					CachedVideoP = CachedVideoP + AfterClustering.sc[(int) neigh[n]].CachedProportion[fid];
				}
				
				if(CachedVideoP >= Full){
					if(AfterClustering.sc[gid].CacheFile[fid]==1){			
						nSelfServed++;
						
						AfterClustering.sc[gid].FileLastTime[fid] = AfterClustering.request[rid].timestamp;
						served_amount[gid] = served_amount[gid] + AfterClustering.sc[gid].CachedProportion[fid];
						Object[] array = AfterClustering.sc[gid].NeighborSC.toArray();
						for(int i=0; i< array.length; i++){
							int index = (int) array[i];
							AfterClustering.sc[index].FileLastTime[fid] = AfterClustering.request[rid].timestamp;
							served_amount[index] = served_amount[index] + AfterClustering.sc[index].CachedProportion[fid];

						}
						
						isSelfServed=true;
						isDownload = false;
					}
					if(!isSelfServed){
						Object[] array = AfterClustering.sc[gid].NeighborSC.toArray();
						for(int i=0; i< array.length; i++){
							int index = (int) array[i];
							if(AfterClustering.sc[index].CacheFile[fid]==1){
								AfterClustering.sc[index].FileLastTime[fid] = AfterClustering.request[rid].timestamp;
								served_amount[index] = served_amount[index] + AfterClustering.sc[index].CachedProportion[fid];
							}
							
							System.out.println("clus = "+index+" Size = "+AfterClustering.sc[index].CachedProportion[fid]);
						}
						
						nNeighborServed++;
						System.out.println("Served Nei");
						isDownload = false;
					}
						
				}
				if(isDownload){
					download_no++;

					nDownload++;				
					System.out.println("Served Dow");
					System.out.println("Group = " + gid);
				
					double filesize = Full/(AfterClustering.sc[gid].NeighborSC.size()+1);
					CacheReplacement(AfterClustering.sc[gid],rid, filesize);
					
					Object[] neighbor = AfterClustering.sc[gid].NeighborSC.toArray();
					for(int n=0; n<AfterClustering.sc[gid].NeighborSC.size(); n++){
						
						if(CachedVideoP <Full){
							CacheReplacement(AfterClustering.sc[(int)neighbor[n]],rid,filesize);
						}
					}
				}
			}
							
				
		}

		bufferedWriter.close();
			
			
			
		
		
		System.out.println("Served : " + nSelfServed);
		System.out.println("Served by neighbor SC : " + nNeighborServed);
		System.out.println("Download : " + nDownload);	
		
		for(int i=0; i< AfterClustering.nClus; i++){
			System.out.println("Clus\t"+ i+"\tServed amount=\t"+ served_amount[i]/Full);
		}
		
		System.out.println("Profile Table:");
		for(int i=0; i<AfterClustering.nClus; i++){
			for(int j=0; j< AfterClustering.m_nEndItemSize; j++){
				System.out.print(TestingClusterFrequency[i][j]+"\t");
			}
			System.out.println();
		}
		System.out.println();
		
		System.out.println("User Count:");
		for(int i=0; i<AfterClustering.nClus; i++){
			System.out.println("clus"+ i+ " : "+ AfterClustering.sc[i].TestUser.size());
		}
		
		
	}
	
	
	
	private static double[][] ReadPopularityMatrix(String input) throws IOException{
		double[][] matrix=new double[AfterClustering.nClus][AfterClustering.m_nEndItemSize];
		BufferedReader br;
		br = new BufferedReader(new FileReader(input));
		
		String line;			
		int i=0;
		while ((line = br.readLine()) != null) {  
			String items[] = line.split("\t");
			System.out.println("Clus" +i+" : ");
			for(int j=0; j<AfterClustering.m_nEndItemSize; j++){
				matrix[i][j]=Integer.valueOf(items[j]);
				System.out.print(matrix[i][j]+"\t");
			}
			System.out.println();
			i++;
		}
		
		
		return matrix;
		
	}
	
	private static double GetPuv(double[] ni, double[] nj, double Timeweight){
		double SimResult = 0.0;
		double Numerator = 0.0;
		
		for(int i=0; i<AfterClustering.m_nEndItemSize; i++){
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
	
	private static void CacheReplacement(AfterClustering.SC sc, int rid,double filesize){
		int Full = AfterClustering.FullVideoSize;
		int fid =AfterClustering.request[rid].VideoID;
			
		boolean isReplace =true;
		double redownloadsize=0;
		if(sc.CacheFile[fid] == 1 && sc.FileLastTime[fid] !=0){
			sc.FileLastTime[fid] = AfterClustering.request[rid].timestamp;

			if(sc.CachedProportion[fid] < filesize){
				if(sc.remaining_space >= (filesize - sc.CachedProportion[fid])){
					redownloadsize = filesize - sc.CachedProportion[fid];
					isReplace = false;	
				}
				else if(sc.remaining_space < (filesize - sc.CachedProportion[fid]) && sc.remaining_space >= 0){
					redownloadsize = sc.remaining_space;
					isReplace = false;	
				}
				else if(sc.remaining_space <= 0){
					isReplace = true;		
				}
				System.out.println("redownload " + redownloadsize);
				CachedVideoP = CachedVideoP +redownloadsize;
				sc.remaining_space = sc.remaining_space - redownloadsize;
				sc.CachedProportion[fid] = sc.CachedProportion[fid]+redownloadsize;					
			}
			else if(sc.CachedProportion[fid] >= filesize){
				isReplace = false;	
			}		
		}
		if(Full-CachedVideoP< filesize){
			filesize = Full-CachedVideoP;
		}
		if(sc.CacheFile[fid] == 0 && sc.remaining_space >= filesize &&CachedVideoP <Full){
		
			sc.CacheFile[fid] = 1;
			sc.FileLastTime[fid] = AfterClustering.request[rid].timestamp;
			CachedVideoP = CachedVideoP +filesize;
			sc.CachedProportion[fid]=filesize;
			sc.remaining_space = sc.remaining_space - filesize;
			
			AfterClustering.file[fid].CachedSC.offer(sc.ID);
			
		}
		else if(isReplace && CachedVideoP <Full){
			
			boolean success = false;
			while (!success){
				int replaceid = -1;
				int oldtime = 999999999;
				for(int i=0; i<sc.CacheFile.length; i++){
					if(sc.FileLastTime[i]!=0 && oldtime >sc.FileLastTime[i]){
						oldtime = sc.FileLastTime[i];
						replaceid = i;
					}
				}
								
				sc.CacheFile[replaceid]=0;
				sc.remaining_space = sc.remaining_space + sc.CachedProportion[replaceid];
				sc.CachedProportion[replaceid]=0;				
				sc.FileLastTime[replaceid]=0;

				Object[] array = AfterClustering.file[replaceid].CachedSC.toArray();
				AfterClustering.file[replaceid].CachedSC.clear();
				for(int a=0; a< array.length; a++){
					if((int) array[a]!=sc.ID /*&& !sc.NeighborSC.contains((int)array[a])*/){
						AfterClustering.file[replaceid].CachedSC.offer((int)array[a]);
					}
				}
				if(sc.remaining_space >= filesize){
					sc.CacheFile[fid] = 1;
					sc.FileLastTime[fid] = AfterClustering.request[rid].timestamp;
					CachedVideoP =CachedVideoP +filesize;
					sc.CachedProportion[fid]=filesize;
					sc.remaining_space = sc.remaining_space - filesize;
					
					AfterClustering.file[fid].CachedSC.offer(sc.ID);
					success = true;
				}
			}
			
		}

	}
	
	
	
}
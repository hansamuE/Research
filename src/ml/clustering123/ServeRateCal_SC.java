package ml.clustering123;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServeRateCal_SC{
	public static void Cal(boolean isCooperative, String NeighborFile) throws NumberFormatException, IOException{
//		int nSCNeighbor =2;
		String popularityinput = "PopularityTrainingPuvSplit0715.txt";
		String type = "puv";
		double[][] ClusSim = new double[AfterClustering.nClus][AfterClustering.nClus];
//		int[][] MaxNSimClus = new int [AfterClustering.nClus][nSCNeighbor];
		
//		int[] ClusRcount = new int[AfterClustering.nClus];
		double[][] fpopularity = new double [AfterClustering.nClus][AfterClustering.m_nEndItemSize];
		double[][] ClusterFrequency =new double[AfterClustering.nClus][AfterClustering.m_nEndItemSize];
		double[][] TestingClusterFrequency =new double[AfterClustering.nClus][AfterClustering.m_nEndItemSize];
		int[] allfpopularity = new int [AfterClustering.m_nEndItemSize];
		
//		for(int rid =0; rid < AfterClustering.m_nEndRequestSize; rid++){
//			ClusRcount[AfterClustering.enduser[AfterClustering.request[rid].UserID].GroupID]++;
//			fpopularity[AfterClustering.enduser[AfterClustering.request[rid].UserID].GroupID][AfterClustering.request[rid].VideoID]++;	
//		}
		
		fpopularity = ReadPopularityMatrix(popularityinput);
		ClusterFrequency = fpopularity;
		for(int i=0; i<AfterClustering.file.length; i++){
			if(AfterClustering.file[i].Fix_ID!=-1){
				allfpopularity[AfterClustering.file[i].Fix_ID] = AfterClustering.file[i].count_duplicate;
			}
		}
		
//		for(int i=0; i<AfterClustering.m_nEndItemSize; i++){
//			System.out.println("Video "+i+": "+allfpopularity[i]);
//		}		
		
		for(int i =0; i < AfterClustering.nClus; i++){
			double sum=0;
			for(int j = 0; j < fpopularity[i].length; j++){
				sum=sum+fpopularity[i][j];
				
			}
			for(int j = 0; j < fpopularity[i].length; j++){
				fpopularity[i][j] = fpopularity[i][j] / sum;  //Normalize
//				System.out.print(fpopularity[i][j]+"\t");
			}
//			System.out.println();			
		}		
		for(int i =0; i < AfterClustering.nClus-1; i++){
//			System.out.print("Clus " + i +"'s sim clus: ");
			for(int j =i+1; j < AfterClustering.nClus; j++){
								
				ClusSim[i][j] = GetPuv(fpopularity[i],fpopularity[j],0);
				System.out.print(ClusSim[i][j]+"\t");
				ClusSim[j][i] = ClusSim[i][j];
			}
			System.out.println();
		}
		
//		for(int i =0; i < AfterClustering.nClus; i++){			
//			double[] TempSim = ClusSim[i];
//			for(int n=0;n<nSCNeighbor;n++){
//				int MaxSim = FindMaxSim(TempSim,i);
//				MaxNSimClus[i][n] = MaxSim;
//				TempSim[MaxSim]=-1;
//				System.out.print(MaxSim+"\t");
//			}
//			
//			System.out.println();			
//		}
		int m_nEndUserSize = AfterClustering.m_nEndUserSize;
//		for(int i=0; i< AfterClustering.nClus; i++){
//     		for(int j=i; j < AfterClustering.nClus;j++){
//     			if(ClusSim[i][j]>0.05){
//     				AfterClustering.sc[i].NeighborSC.offer(j);
//     				AfterClustering.sc[j].NeighborSC.offer(i);     				
//     			}   
//     		}     		
//		}
		
		
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
		
	
//		for(int i =0; i < AfterClustering.nClus; i++){
//			System.out.print("Clus " + i +"'s sim clus: ");
//			for(int j =i+1; j < AfterClustering.nClus; j++){
//				ClusSim[i][j] = GetCosSim(fpopularity[i],fpopularity[j]);
//				ClusSim[j][i] = ClusSim[i][j];
//			}
//			double[] TempSim = ClusSim[i];
//			for(int n=0;n<nSCNeighbor;n++){
//				int MaxSim = FindMaxSim(TempSim);
//				MaxNSimClus[i][n] = MaxSim;
//				TempSim[MaxSim]=-1;
//				System.out.print(MaxSim+"\t");
//			}
//			
//			System.out.println();			
//		}
		
		
		ClusterFrequency = ReadPopularityMatrix(popularityinput);
		
		//////////////////////////////////////////////////////////////////////////
		
		double[] served_amount = new double[AfterClustering.nClus];
		double[] served_neighbor_amount = new double[AfterClustering.nClus];
//		double[] CachedVideoP= new double[AfterClustering.m_nEndItemSize];
		AfterClustering.CachedVideoP= new double[AfterClustering.m_nEndItemSize];
		int Full=30;
		int nSelfServed = 0;
		int nNeighborServed = 0;
		int nDownload = 0;		
		for(int rid =0; rid < AfterClustering.m_nEndRequestSize; rid++){	
			boolean isSelfServed = false;
			boolean isDownload = true;
			int uid = AfterClustering.request[rid].UserID;
			int fid = AfterClustering.request[rid].VideoID;
			int gid = AfterClustering.enduser[uid].GroupID;
			
			///
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
//					sim = GetPuv(simmatrix,ClusterFrequency[j],0);
					if(type == "puv"){
						sim = GetPuv(simmatrix,fpopularity[j],0);
					}
					else if(type == "cos"){
						for(int k=0; k<AfterClustering.m_nEndItemSize; k++){
							sim=sim + simmatrix[k]*ClusterFrequency[j][k];		
//							System.out.println(sim);
						}	
					}
								
					if(sim > maxvalue*1.3){
						maxvalue = sim;
						maxclus =j;
//						System.out.println("maxclus=" +maxclus);
					}
				}
				gid=maxclus;
				
//				System.out.println("Assign to "+maxclus);			
				
			}
			
			
			
			
			///
			
			
			if(gid != -1){
				if(!AfterClustering.sc[gid].TestUser.contains(uid)){
					AfterClustering.sc[gid].TestUser.offer(uid);
				}
				TestingClusterFrequency[gid][fid]++;
				
				
//				System.out.println("video"+fid+" : "+AfterClustering.CachedVideoP[fid]+"in the cache");
				if(AfterClustering.CachedVideoP[fid] >= Full){
						if(AfterClustering.sc[gid].CacheFile[fid]==1){				
//					if(AfterClustering.sc[gid].CachedFile.contains(fid)){
					//if(AfterClustering.sc[gid].cache.containsValue(fid)){
							AfterClustering.sc[gid].FileLastTime[fid] = AfterClustering.request[rid].timestamp;			
							AfterClustering.sc[gid].ClickTime[fid]++;
							Object[] array = AfterClustering.file[fid].CachedSC.toArray();
							for(int i=0; i< array.length; i++){
								int index = (int) array[i];
								AfterClustering.sc[index].FileLastTime[fid] = AfterClustering.request[rid].timestamp;
								AfterClustering.sc[index].ClickTime[fid]++;
								served_amount[index] = served_amount[index] + AfterClustering.sc[index].CachedProportion[fid];
//								System.out.println("Served Self, Clus = "+ index+" Size = "+AfterClustering.sc[index].CachedProportion[fid]);
							}
//							served_amount[gid]+=AfterClustering.sc[gid].CachedProportion[fid];
							nSelfServed++;
							isSelfServed=true;
							isDownload = false;
//							break;
						}
//					}
					if(!isSelfServed){
						Object[] array = AfterClustering.file[fid].CachedSC.toArray();
						for(int i=0; i< array.length; i++){
							int index = (int) array[i];
//							AfterClustering.sc[index].FileLastTime[fid] = AfterClustering.request[rid].timestamp;
//							served_amount[index] = served_amount[index] + AfterClustering.sc[index].CachedProportion[fid];
							served_neighbor_amount[index]=served_neighbor_amount[index]+ AfterClustering.sc[index].CachedProportion[fid];
//							System.out.println("Served Nei, clus = "+index+" Size = "+AfterClustering.sc[index].CachedProportion[fid]);
						}
						
						nNeighborServed++;
//						System.out.println("Served Nei");
						isDownload = false;
					}
						
				}
				if(isDownload){
					nDownload++;				
//					System.out.println("Served Dow");
//					System.out.println("file = " + fid);
//					System.out.println("Group = " + gid);
				
					//AfterClustering.sc[gid].cache.put(fid, fid);
//					 System.out.println("\rSize = " + AfterClustering.sc[gid].cache.size() + "\tCurrent value = " + fid);  
//					AfterClustering.sc[gid].CachedFile.offer(fid);
					double filesize = Full/(AfterClustering.sc[gid].NeighborSC.size()+1);
//					if(Full-AfterClustering.CachedVideoP[fid]< filesize){
//						filesize = Full-AfterClustering.CachedVideoP[fid];
//					}
					CacheReplacement(AfterClustering.sc[gid],rid, filesize);
					
					Object[] neighbor = AfterClustering.sc[gid].NeighborSC.toArray();
					for(int n=0; n<AfterClustering.sc[gid].NeighborSC.size(); n++){
//						if(Full-AfterClustering.CachedVideoP[fid]< filesize){
//							filesize = Full-AfterClustering.CachedVideoP[fid];
//						}
//						AfterClustering.sc[MaxNSimClus[gid][n]].CachedFile.offer(fid);
						if(AfterClustering.CachedVideoP[fid] < Full){
							CacheReplacement(AfterClustering.sc[(int)neighbor[n]],rid,filesize);
						}
//						AfterClustering.sc[MaxNSimClus[gid][n]].cache.put(fid, fid);
//						System.out.println("Neighbor = " + MaxNSimClus[gid][n] );
//						System.out.println("\rSize = " + AfterClustering.sc[MaxNSimClus[gid][n]].cache.size() + "\tCurrent value = " + fid);  
					}
//					AfterClustering.CachedVideoP[fid]=Full;   //�n�R��
//					System.out.println();
				}
			}
			
	
		}
		
		for(int i =0; i < AfterClustering.nClus-1; i++){
//			System.out.print("Clus " + i +"'s sim clus: ");
			for(int j =i+1; j < AfterClustering.nClus; j++){
								
				ClusSim[i][j] = GetPuv(fpopularity[i],fpopularity[j],0);
				System.out.print(ClusSim[i][j]+"\t");
				ClusSim[j][i] = ClusSim[i][j];
			}
			System.out.println();
		}
			
			
			
		
		
		System.out.println("Served : " + nSelfServed);
		System.out.println("Served by neighbor SC : " + nNeighborServed);
		System.out.println("Download : " + nDownload);	
		
		for(int i=0; i< AfterClustering.nClus; i++){
			System.out.println("Clus\t"+ i+"\tServed amount=\t"+ served_amount[i]/30);
		}
		for(int i=0; i< AfterClustering.nClus; i++){
			System.out.println("Clus\t"+ i+"\tServed neibor amount=\t"+ served_neighbor_amount[i]/30);
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
		
		
			
//		System.out.println("fid = " +sc.CacheFile[fid]);
		int removedid = -1;
		boolean isReplace =true;
		double redownloadsize=0;
		if(sc.CacheFile[fid] == 1 && sc.FileLastTime[fid] !=0){
			sc.FileLastTime[fid] = AfterClustering.request[rid].timestamp;
			sc.ClickTime[fid]++;
//			System.out.println(fid+ " is find in "+sc.ID+" "+sc.FileLastTime[fid]);
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
//				System.out.println("redownload " + redownloadsize);
				AfterClustering.CachedVideoP[fid] = AfterClustering.CachedVideoP[fid] +redownloadsize;
				sc.remaining_space = sc.remaining_space - redownloadsize;
				sc.CachedProportion[fid] = sc.CachedProportion[fid]+redownloadsize;					
			}
			else if(sc.CachedProportion[fid] >= filesize){
				isReplace = false;	
			}				
		}
		
		if(Full-AfterClustering.CachedVideoP[fid]< filesize){
			filesize = Full-AfterClustering.CachedVideoP[fid];
		}
		
		if(sc.CacheFile[fid] == 0 && sc.remaining_space >= filesize && AfterClustering.CachedVideoP[fid] <Full){
			
			sc.CacheFile[fid] = 1;
//			System.out.println(fid+" Cache!");
			sc.FileLastTime[fid] = AfterClustering.request[rid].timestamp;
			sc.ClickTime[fid]++;
			AfterClustering.CachedVideoP[fid] = AfterClustering.CachedVideoP[fid] +filesize;
			sc.CachedProportion[fid]=filesize;
			sc.remaining_space = sc.remaining_space - filesize;
			
			AfterClustering.file[fid].CachedSC.offer(sc.ID);
			
			isReplace = false;						
		}		
		else if(isReplace && AfterClustering.CachedVideoP[fid] <Full){
			
//			System.out.println("Replace!");
			boolean success = false;
			boolean stop = false;
			while (!success){
				if(stop) break;
				int replaceid = -1;
				int oldtime = 999999999;
				
				for(int i=0; i<sc.CacheFile.length; i++){
					
					if(AfterClustering.Replace == "LRU" && sc.FileLastTime[i]!=0 && oldtime >sc.FileLastTime[i]){
						oldtime = sc.FileLastTime[i];
						replaceid = i;
					}
					else if (AfterClustering.Replace == "LFU" && sc.FileLastTime[i]!=0 && oldtime >sc.ClickTime[i]){
						oldtime = sc.ClickTime[i];
						replaceid = i;
					}
					
				}
//				if(replaceid <10){
//					stop =true;
//				}
				if(! stop){
					Object[] array = AfterClustering.file[replaceid].CachedSC.toArray();
					AfterClustering.CachedVideoP[replaceid] = AfterClustering.CachedVideoP[replaceid] - sc.CachedProportion[replaceid];
					sc.CacheFile[replaceid]=0;
					sc.remaining_space = sc.remaining_space + sc.CachedProportion[replaceid];
					sc.CachedProportion[replaceid]=0;				
					sc.FileLastTime[replaceid]=0;				
//					if(AfterClustering.CachedVideoP[replaceid]>=Full){
						AfterClustering.file[replaceid].CachedSC.clear();
						for(int a=0; a< array.length; a++){
							if((int) array[a]!=sc.ID){
								AfterClustering.file[replaceid].CachedSC.offer((int)array[a]);
							}
						}				
//					}				
//					else if(AfterClustering.CachedVideoP[replaceid]<Full){					
//						for(int i=0; i< array.length; i++){
//							int index = (int) array[i];
//							if(index != sc.ID){
//								AfterClustering.CachedVideoP[replaceid] = AfterClustering.CachedVideoP[replaceid] - AfterClustering.sc[index].CachedProportion[replaceid];
//								AfterClustering.sc[index].CacheFile[replaceid]=0;
//								AfterClustering.sc[index].remaining_space =AfterClustering.sc[index].remaining_space + AfterClustering.sc[index].CachedProportion[replaceid];
//								AfterClustering.sc[index].CachedProportion[replaceid]=0;				
//								AfterClustering.sc[index].FileLastTime[replaceid]=0;
//							}
//						}
//						AfterClustering.file[replaceid].CachedSC.clear();
//					}
					
					
					System.out.println("remove" + replaceid);
				}
				
				
				
				if(sc.remaining_space >= filesize){
					sc.CacheFile[fid] = 1;
					System.out.println(fid+"cached");
					sc.FileLastTime[fid] = AfterClustering.request[rid].timestamp;
					AfterClustering.CachedVideoP[fid] = AfterClustering.CachedVideoP[fid] +filesize;
					sc.CachedProportion[fid]=filesize;
					sc.remaining_space = sc.remaining_space - filesize;
					
					AfterClustering.file[fid].CachedSC.offer(sc.ID);
					success = true;
				}
			}
			
		}
		
//		return removedid;
		
	}
	
	
	
	private static double GetCosSim(int[] ni, int[] nj){
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
	
	private static int FindMaxSim(double[] Clus, int ClusNum){
		int MaxN=-1;
		double SimTemp=-1;
		for(int i=0; i<Clus.length; i++){
			if(i!=ClusNum && Clus[i]> SimTemp){
				SimTemp = Clus[i];
				MaxN = i;
			}
		}
		return MaxN;
	}
}
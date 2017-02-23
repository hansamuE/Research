package ml.clustering123;

import java.io.IOException;

public class ServeRateCal_user{
	public static void Cal() throws NumberFormatException, IOException{
		
		double average_neighbor = 0;
		int num_served=0;
		int num_neighdown=0;
		int num_download = 0;		
		
	
		for(int rid =0; rid < ReadFile.m_nEndRequestSize; rid++){
			for(Integer q : ReadFile.enduser[ReadFile.request[rid].UserID].Neighbor){
				boolean isDelete = true;
				//System.out.println("q= "+q);
				while (!ReadFile.enduser[q].FileEndTime.isEmpty() && isDelete){
					//int fileendtime= (int) enduser[q].FileEndTime.peek();
					//System.out.print("element : ");
					//System.out.println(enduser[q].FileEndTime.element());
					if(ReadFile.enduser[q].FileEndTime.element() < ReadFile.request[rid].timestamp){
						ReadFile.enduser[q].FileEndTime.poll();
						ReadFile.enduser[q].ExistFile.poll();
					}
					else{
						isDelete = false;
						
					}
				
				}
				//System.out.println();
				
			}
			boolean isDelete = true;
			while (!ReadFile.enduser[ReadFile.request[rid].UserID].FileEndTime.isEmpty() && isDelete){
				//int fileendtime= (int) enduser[q].FileEndTime.peek();
				//System.out.print("element : ");
				//System.out.println(enduser[request[rid].UserID].FileEndTime.element());
				if(ReadFile.enduser[ReadFile.request[rid].UserID].FileEndTime.element() < ReadFile.request[rid].timestamp){
					ReadFile.enduser[ReadFile.request[rid].UserID].FileEndTime.poll();
					ReadFile.enduser[ReadFile.request[rid].UserID].ExistFile.poll();
					}
				else{
					isDelete = false;
					
				}
				
			}		
			boolean isServed = false;
			for(Integer q : ReadFile.enduser[ReadFile.request[rid].UserID].ExistFile){
				if(q == ReadFile.request[rid].VideoID){
					System.out.println("Served");
					num_served++;
					isServed=true;
					break;
					
				}
				
			}
			if(!isServed){
				for(Integer qu : ReadFile.enduser[ReadFile.request[rid].UserID].Neighbor){
					if(isServed){
						break;
					
					}
					for(Integer qf : ReadFile.enduser[qu].ExistFile){
						if(qf ==  ReadFile.request[rid].VideoID){
							System.out.println("Download from neighbor" + qu);
							num_neighdown++;
							isServed=true;
							break;
						}
						
					}	
					
				}
				if(!isServed){
					System.out.println("Download¡I");
					average_neighbor = average_neighbor + ReadFile.enduser[ReadFile.request[rid].UserID].Neighbor.size();
					for(Integer q : ReadFile.enduser[ReadFile.request[rid].UserID].Neighbor){
						ReadFile.enduser[q].ExistFile.offer(ReadFile.request[rid].VideoID);
						ReadFile.enduser[q].FileEndTime.offer(ReadFile.request[rid].timestamp+ReadFile.videolength);
					}										
					num_download++;
				}
			}
							
			ReadFile.enduser[ReadFile.request[rid].UserID].ExistFile.offer(ReadFile.request[rid].VideoID);	
			ReadFile.enduser[ReadFile.request[rid].UserID].FileEndTime.offer(ReadFile.request[rid].timestamp+ReadFile.videolength);
			System.out.println(ReadFile.request[rid].timestamp+"\t"+ReadFile.request[rid].UserID+"\t"+ReadFile.request[rid].VideoID);
			System.out.print("Exist File : ");
			//for(Integer v : enduser[request[rid].UserID].ExistFile){
			System.out.print(ReadFile.enduser[ReadFile.request[rid].UserID].ExistFile);//.element()+"\t");								
			//}
			System.out.println();
			System.out.print("EndTime : ");
			System.out.println(ReadFile.enduser[ReadFile.request[rid].UserID].FileEndTime);
			
			
		
		}
		

	System.out.println("Served : " + num_served);
	System.out.println("Served by neighbor: " + num_neighdown);
	System.out.println("Download : " + num_download); 	
	System.out.println("Total neighbor : " + average_neighbor);
	}
}
		
		
		
		
		
		
		
//		
//		
//		ReadFile.Request[] request = new ReadFile.Request[ReadFile.m_nRequest];
//		for(int i=0;i<ReadFile.m_nRequest; i++){
//			request[i] = new ReadFile.Request();
//		}		
//		double average_neighbor = 0;
//		int num_served=0;
//		int num_neighdown=0;
//		int num_download = 0;
//		
//		String line;
//		BufferedReader br;
//		br = new BufferedReader(new FileReader(inputfile));
//		int rid=0;
//		while ((line = br.readLine()) != null) {
//			String items[] = line.split(" "); // #Timestamp  #YouTube server IP #Client IP    #Request #Video ID   #Content server IP
//         	
//			for(int i = 0; i < ReadFile.user.length; i++){
//				if(request[rid].ID != -1) break;
//				if(items[2].equals(ReadFile.user[i].Name)){
//					if(ReadFile.user[i].Fix_ID==-1) break;
//					for(int j=0; j<ReadFile.file.length; j++){
//						if(items[4].equals(ReadFile.file[j].Name)){
//							if(ReadFile.file[j].Fix_ID==-1){break;}
//							else{
//								request[rid].ID=rid;
//								String str=(String) items[0].subSequence(4,10);
//								//System.out.println(str);
//								request[rid].timestamp=Integer.valueOf(str);
//							
//								request[rid].UserID=ReadFile.user[i].Fix_ID;
//								request[rid].VideoID=ReadFile.file[j].Fix_ID;						
//						
//								//System.out.print("(before) EndTime : ");
//								//System.out.println(enduser[request[rid].UserID].FileEndTime);
//							
//								for(Integer q : ReadFile.enduser[request[rid].UserID].Neighbor){
//									boolean isDelete = true;
//									//System.out.println("q= "+q);
//									while (!ReadFile.enduser[q].FileEndTime.isEmpty() && isDelete){
//										//int fileendtime= (int) enduser[q].FileEndTime.peek();
//									
//										//System.out.print("element : ");
//										//System.out.println(enduser[q].FileEndTime.element());
//										if(ReadFile.enduser[q].FileEndTime.element() < request[rid].timestamp){
//											ReadFile.enduser[q].FileEndTime.poll();
//											ReadFile.enduser[q].ExistFile.poll();
//										}
//										else{
//											isDelete = false;
//										}
//									}			
//									//System.out.println();
//								
//									
//								}
//							
//								
//							
//								boolean isDelete = true;
//								while (!ReadFile.enduser[request[rid].UserID].FileEndTime.isEmpty() && isDelete){
//									//int fileendtime= (int) enduser[q].FileEndTime.peek();
//								
//									//System.out.print("element : ");
//									//System.out.println(enduser[request[rid].UserID].FileEndTime.element());
//									if(ReadFile.enduser[request[rid].UserID].FileEndTime.element() < request[rid].timestamp){
//										ReadFile.enduser[request[rid].UserID].FileEndTime.poll();
//										ReadFile.enduser[request[rid].UserID].ExistFile.poll();
//									}
//									else{
//										isDelete = false;
//									}
//								}		
//								boolean isServed = false;
//								for(Integer q : ReadFile.enduser[request[rid].UserID].ExistFile){
//									if(q == request[rid].VideoID){
//										System.out.println("Served");
//										num_served++;
//										isServed=true;
//										break;
//									}
//								}
//								if(!isServed){
//									for(Integer qu : ReadFile.enduser[request[rid].UserID].Neighbor){
//										if(isServed){
//											break;
//										}
//										for(Integer qf : ReadFile.enduser[qu].ExistFile){
//											if(qf ==  request[rid].VideoID){
//												System.out.println("Download from neighbor" + qu);
//												num_neighdown++;
//												isServed=true;
//												break;
//											}
//										}	
//										
//									}
//									if(!isServed){
//										System.out.println("Download¡I");
//										average_neighbor = average_neighbor + ReadFile.enduser[request[rid].UserID].Neighbor.size();
//										for(Integer q : ReadFile.enduser[request[rid].UserID].Neighbor){
//											ReadFile.enduser[q].ExistFile.offer(request[rid].VideoID);
//											ReadFile.enduser[q].FileEndTime.offer(request[rid].timestamp+ReadFile.videolength);
//										}										
//										num_download++;
//									}
//								}
//							
//								ReadFile.enduser[request[rid].UserID].ExistFile.offer(request[rid].VideoID);	
//								ReadFile.enduser[request[rid].UserID].FileEndTime.offer(request[rid].timestamp+ReadFile.videolength);
//							
//								System.out.println(request[rid].timestamp+"\t"+request[rid].UserID+"\t"+request[rid].VideoID);
//								System.out.print("Exist File : ");
//								//for(Integer v : enduser[request[rid].UserID].ExistFile){
//								System.out.print(ReadFile.enduser[request[rid].UserID].ExistFile);//.element()+"\t");								
//								//}
//								System.out.println();
//								System.out.print("EndTime : ");
//								System.out.println(ReadFile.enduser[request[rid].UserID].FileEndTime);
//							
//							
//							
//								rid++;
//								break;
//							}
//						}
//					}
//				}
//			}
//			
//		}
//		
//		System.out.println("num_Video = " + rid);
//		
//		System.out.println("Served : " + num_served);
//		System.out.println("Served by neighbor: " + num_neighdown);
//		System.out.println("Download : " + num_download); 	
//		
//		System.out.println("Total neighbor : " + average_neighbor);
//		
//		
//	}
//	
//}

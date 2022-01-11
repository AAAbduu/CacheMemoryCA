package cacheSim;

import java.util.Scanner;

public class CacheSim {
	
	private static void printCM(CacheM eCache) {
		String [][][] auxCache = eCache.getMem();
		System.out.println(" Busy | Dirty | Tag | Repl. || Data");
		//System.out.println("---------------------------------------");
		for(int s=0; s<eCache.nSets();s++) {
		for(int i=0;i<eCache.getlSize();i++) {
			if(i%eCache.getlSize()==0) {
				System.out.println("---------------------------------------");
			}
			for(int j=0; j<5;j++) {
				if(j==0) {
				System.out.print("   "+auxCache[s][i][j]);
				
				}else if(j<4) {
					System.out.print("      "+auxCache[s][i][j]);
				}
				if(j==4) {
					System.out.print("   ||   "+auxCache[s][i][j]+"\n");
				}
			}
		}
	}
		
		System.out.println("---------------------------------------");
		
	}

	public static void main(String[] args) {
		
		int state=1;
		Scanner input = new Scanner(System.in); 
		
		
		
		
		System.out.println("CACHE MEMORY SIMULATOR");
		
		System.out.println("--------------------------------------------------------------");
			
		System.out.print("Please select word size (4-8 bytes): ");
		int wordSize = input.nextInt();
		System.out.print("Block/Line size (32-64 bytes): ");
		int blockSize = input.nextInt();
		System.out.print("Number of lines per set: 1 (direct), 2, 4, or 8 (fully associative): ");
		int setSize = input.nextInt();
		
		System.out.print("Replacement pol. (0(FIFO) - 1(LRU)): ");
		int replacePol = input.nextInt();
		
		CacheM prueba = new CacheM(blockSize,wordSize,setSize,replacePol);
		printCM(prueba);
		boolean execution  = true;
		while(execution) {
		
		switch(state) {
			case 1:
					
					System.out.print("Mem. address (byte) (-1 to finish): ");
					int memAddr = input.nextInt();
					if(memAddr==-1) {
						state=2;
						break;
					}
					System.out.print("Load(0)/Store(1): ");
					int op = input.nextInt();
					prueba.printInfo(memAddr);
					
					int[] data = prueba.calculateMem(memAddr);
					prueba.HorM(data);
						if(prueba.getrPol()==1&& prueba.nSets()!=8) { 
							if(op==0) {
								if(prueba.alreadyInSet(data)&&prueba.isLRU(memAddr)==false) {//Update lru if data is in cache and block is not lru.
									prueba.LRU(memAddr);
								
								}else if (prueba.alreadyInSet(data)==false){
									prueba.updateCLRU(memAddr);
								}
								
							}else if(op==1){
								if(prueba.alreadyInSet(data)) {
									prueba.LRU(memAddr);
									prueba.dirtyBit(memAddr);
								}else if(prueba.alreadyInSet(data)==false) {
									prueba.updateCLRU(memAddr);
									prueba.dirtyBit(memAddr);
								}
								
							
								//Update policy
							}
						}else if(prueba.getrPol()==0&& prueba.nSets()!=8){	
						
							if(op==0 ) {
								
								 prueba.updateFIFO(memAddr);
							}else if(op==1 && prueba.alreadyInSet(data)|| (op==1 && !prueba.alreadyInSet(data))){
								prueba.dirtyBit(memAddr);
								//No update of repl policy needed.
							}
						}
					else if(prueba.getrPol()==1 || prueba.getrPol()==0 && prueba.nSets()==8){
						
						prueba.updateDirect(memAddr,op);
					}
						
					
					
					printCM(prueba);
					break;
					
			case 2:
				System.out.println("You have finshed the execution, here are your results: ");
				execution=false;
				double [] fdata = prueba.globalH();
				float hits = CacheM.getHits();
				float access = CacheM.getAccesses();
				float rate = hits/access;
				System.out.println("Ref: " +CacheM.getAccesses()+  "-- Hits: "+fdata[0]+" -- Hit rate: "+ rate);
				System.out.println("Total Access Time: "+ fdata[2]+" cycles.");
				break;
					
					}
		}
		
				
					
					
					
			
					
					
		

	}

		

}


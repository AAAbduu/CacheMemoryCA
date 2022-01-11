package cacheSim;
//Write Back policy.
public class CacheM {
	private String[][][] mem;
	private int bSize; // block word Size
	private int wSize; //Word Size
	private int lSize; // line size
	private int rPol; //0 for FIFO, 1 for LRU
	private static int accesses;
	private static int cycles;
	private static int hits;
	
	
	
	
	
	public static int getAccesses() {
		return accesses;
	}


	public static int getHits() {
		return hits;
	}


	public static void setHits(int hits) {
		CacheM.hits = hits;
	}


	public CacheM(int eBSize, int eWSize, int eLSize, int eRPol) {
		this.bSize=eBSize;
		this.wSize=eWSize;
		this.lSize=eLSize;
		this.rPol=eRPol;
		
		this.mem = new String[this.nSets()][8][5];
		this.initialize();
		
	}
	
	
	public String[][][] getMem() {
		return this.mem;
	}




	public int getbSize() {
		return bSize;
	}



	public void setbSize(int bSize) {
		this.bSize = bSize;
	}



	public int getwSize() {
		return wSize;
	}



	public void setwSize(int wSize) {
		this.wSize = wSize;
	}



	public int getlSize() {
		return lSize;
	}



	public void setlSize(int lSize) {
		this.lSize = lSize;
	}

	public int getRows() {
		return this.bSize/this.wSize;
	}


	public int getrPol() {
		return rPol;
	}



	public void setrPol(int rPol) {
		this.rPol = rPol;
	}

	private void initialize() {
		for(int s=0;s<this.nSets();s++) {
			for(int i=0;i<8;i++) {
				for(int j=0; j<5;j++) {
				
				this.getMem()[s][i][j]="0";
				
				
			}
		}
	}
}

	
	
	public int getWord(int memAddr) {
		return memAddr/this.wSize;
	}
	
	public int getBlock(int memAddr) {
		return memAddr/this.bSize;
	}
	
	public int nSets() {
		return 8/this.lSize;
	}
	
	public int getThisSet(int memAddr) {
		return this.getBlock(memAddr)%this.nSets();
	}
	
	public int getThisTag(int memAddr) {
		return this.getBlock(memAddr)/this.nSets();
	}
	
	public int[] calculateMem(int memAddr) {
		int [] data = new int[4];
		int word = this.getWord(memAddr);
		int block = this.getBlock(memAddr);
		int nSets = this.nSets();
		int set = this.getThisSet(memAddr);
		int tag = this.getThisTag(memAddr);
		
		
		data[0]=block;
		data[1]=set;
		data[2]=tag;
		data[3]=this.getlSize()-1;
		
		return data;
	}


	public void printInfo(int memAddr) {
		int word = this.getWord(memAddr);
		int block = this.getBlock(memAddr);
		int nSets = this.nSets();
		int set = this.getThisSet(memAddr);
		int tag = this.getThisTag(memAddr);
		int wBlock = (this.getbSize()/this.getwSize()); 
		System.out.print("Word: "+word+" - Block: "+block);
		if(memAddr%this.getwSize()==0) {
		System.out.println(" - (words "+ (int)(Math.floor(word))	+"-"+(int)((word)+(wBlock-1))+")");
		}else {
			System.out.println(" - (words "+ (int)(Math.floor(memAddr/this.getwSize()))	+"-"+(int)(Math.floor(memAddr/this.getwSize())+wBlock-1)+")");
		}
		if(this.getlSize()==8 || this.getlSize()==1) {
		System.out.println("Tag: "+tag);
		}else {
			System.out.println("Set: " + set+" Tag: "+tag);
		}
	}
	
	public void updateCLRU(int memAddr) {
		
		int [] lineData = calculateMem(memAddr);
		
		String [][][] cache =this.getMem();
		
		for(int i = 0;i<this.getlSize();i++) {//Non busy exists
			if(!(this.alreadyInSet(lineData))) {
				if(/*this.nonBusyInSet(lineData[1])&&*/cache[lineData[1]][i][0].contentEquals("0")){
				cache[lineData[1]][i][0]="1";  //Now its busy	
				cache[lineData[1]][i][2]=String.valueOf(lineData[2]); //Writing tag
				this.updateRP(lineData, i);
				
				cache[lineData[1]][i][1]="0";	//Writing dirty bit to 0 when data is loaded.
				cache[lineData[1]][i][4]="B"+String.valueOf(lineData[0]);	//Writing block data
				
				i=this.getlSize();
					}
				}
			}
		
		for(int i = 0;i<this.getlSize();i++) { //Every line is busy so LRU repl policy acts.
			if(!(this.alreadyInSet(lineData))) {
				if(!this.nonBusyInSet(lineData[1])&&cache[lineData[1]][i][0].contentEquals("1")){
					int aux=Integer.parseInt(this.getMem()[lineData[1]][i][3],2);
					if(aux==0) {
						cache[lineData[1]][i][2]=String.valueOf(lineData[2]); //Writing tag
						this.updateRP(lineData, i);
						
						cache[lineData[1]][i][1]="0";	//Writing dirty bit to 0 when data is loaded.
						cache[lineData[1]][i][4]="B"+String.valueOf(lineData[0]);	//Writing block data
						i=this.getlSize();
					}
				}
			}
		}
	
	}
	
	public boolean inSet(int [] data) {
		for(int i=0;i<this.getlSize();i++) {
			if(this.getMem()[data[1]][i][4].contentEquals("B"+String.valueOf(data[0]))) {
				
				return true;			//It is contained in the set so we stop search.
			}
		
	}return false;		// Data is not in set.
	}
	
	public boolean isLRU(int memAddr) {
		int [] data = this.calculateMem(memAddr);
		for(int i =0; i<this.getlSize();i++) {
			if(this.getMem()[data[1]][i][4].contentEquals("B"+String.valueOf(data[0]))&&this.getMem()[data[1]][i][3].contentEquals(String.valueOf(this.getlSize()-1))) {
				return true;
			}
		}return false;
	}
	
	
	public void updateFIFO(int memAddr) {
		int [] lineData = calculateMem(memAddr);
		
		
		String [][][] cache =this.getMem();
		
		for(int i = 0;i<this.getlSize();i++) {//Non busy exists
			if(!(this.alreadyInSet(lineData))) {
				if(/*this.nonBusyInSet(lineData[1])&&*/cache[lineData[1]][i][0].contentEquals("0")){
				cache[lineData[1]][i][0]="1";  //Now its busy	
				cache[lineData[1]][i][2]=String.valueOf(lineData[2]); //Writing tag
				
				cache[lineData[1]][i][1]="0";	//Writing dirty bit to 0 when data is loaded.
				this.updateRP(lineData, i);
				cache[lineData[1]][i][4]="B"+String.valueOf(lineData[0]);	//Writing block data
				i=this.getlSize();
					}
				}
			}
		
		for(int i = 0;i<this.getlSize();i++) { //Every line is busy so FIFO repl policy acts.
			if(!(this.alreadyInSet(lineData))) {
				if(!this.nonBusyInSet(lineData[1])&&cache[lineData[1]][i][0].contentEquals("1")){
					int aux=Integer.parseInt(this.getMem()[lineData[1]][i][3],2);
					if(aux==0) {
						cache[lineData[1]][i][2]=String.valueOf(lineData[2]); //Writing tag
						
						cache[lineData[1]][i][1]="0";	//Writing dirty bit to 0 when data is loaded.
						this.updateRP(lineData, i);
						cache[lineData[1]][i][4]="B"+String.valueOf(lineData[0]);	//Writing block data
						i=this.getlSize();
					}
				}
			}
		}
	}
	
	//If replacement policy is FIFO, nothing changes, if it is LRU, repl pol bits must be updated.
	public void dirtyBit(int memAddr) { //Store instruction updates dirty bit. Previous condition in where invoked is that operation must be store.
		int [] lineData = calculateMem(memAddr);
		this.updateCLRU(memAddr);
			for(int i=0;i<this.getlSize();i++) {
				if(this.getMem()[lineData[1]][i][4].contentEquals("B"+String.valueOf(lineData[0]))) {
					this.getMem()[lineData[1]][i][1]="1"; // Data became dirty because its written on cache but not in MM.
				}
			}
			
	}
	
	
	
	
	public void LRU(int memAddr) {
		int [] data = this.calculateMem(memAddr);
		int pos=0;
		int max=0; 
		for(int i = 0;i<this.getlSize();i++) {
				if(this.getMem()[data[1]][i][4].contentEquals("B"+data[0])) { //If line is busy
					max = Integer.parseInt(this.getMem()[data[1]][i][3],2);
					this.getMem()[data[1]][i][3]=Integer.toBinaryString(this.getlSize()-1);
					pos=i;
					i = this.getlSize();
				}
		}for(int i = 0;i<this.getlSize();i++) {
			if(this.getMem()[data[1]][i][0].contentEquals("1")&&i!=pos&&max<Integer.parseInt(this.getMem()[data[1]][i][3],2)) { //If line is busy
				int aux=Integer.parseInt(this.getMem()[data[1]][i][3],2);  //transform current binary policy number into integer
				if(aux>0) {
				this.getMem()[data[1]][i][3]=Integer.toBinaryString(aux-1); //add -1 to this number and rewrite it.
				}
			}
			
		}	
	}
	
	
	
	public boolean alreadyInSet(int [] lineData) {
		for(int i=0;i<this.getlSize();i++) {
				if(this.getMem()[lineData[1]][i][4].contentEquals("B"+String.valueOf(lineData[0]))) {
					
					return true;			//It is contained in the set so we stop search.
				}
			
		}return false;		// Data is not in set.
	}
	
	
	
	public boolean nonBusyInSet(int set) {
		for(int i=0; i<this.getlSize();i++) {
			if(this.getMem()[set][i][0].contentEquals("0")){
				return true;		//there is a non busy line in set
			}
		}return false;		//every line is busy on set
	}
	
	
	
	
	public void updateRP(int [] data,int x) {
			this.getMem()[data[1]][x][3]=Integer.toBinaryString(data[3]); //Writing repl pol.
			for(int i = 0;i<this.getlSize();i++) {
					if(this.getMem()[data[1]][i][0].contentEquals("1")&&i!=x) { //If line is busy
						int aux=Integer.parseInt(this.getMem()[data[1]][i][3],2);  //transform current binary policy number into integer
						if(aux>0) {
						this.getMem()[data[1]][i][3]=Integer.toBinaryString(aux-1); //add -1 to this number and rewrite it.
						}
					
				}	
			
			}
		}
	
	public void updateDirect(int memAddr, int op) {
		int [] data = this.calculateMem(memAddr);
		if(op==1) {
			this.getMem()[data[1]][0][4]=String.valueOf("B"+data[0]);
			this.getMem()[data[1]][0][2]=String.valueOf(data[2]);
			this.getMem()[data[1]][0][1]="1";
			this.getMem()[data[1]][0][0]=String.valueOf("1");
			
		}else {
		
		this.getMem()[data[1]][0][2]=String.valueOf(data[2]);
		if(this.getMem()[data[1]][0][4].contentEquals("B"+data[0])&&this.getMem()[data[1]][0][1].contentEquals("1"))
		this.getMem()[data[1]][0][1]="1";
		else {this.getMem()[data[1]][0][1]="0";}
		this.getMem()[data[1]][0][0]=String.valueOf("1");
		this.getMem()[data[1]][0][4]=String.valueOf("B"+data[0]);
		}
		
	
	}
	
	public void HorM(int [] data) {
		if(this.alreadyInSet(data)) {
			System.out.println("HIT");
			
			System.out.println("Access Time: "+ 2+" cycles");
			CacheM.hits++;
			CacheM.accesses++;
			CacheM.cycles=CacheM.cycles+2;
		}else {
			System.out.println("MISS");
			int size = ((this.getbSize()/this.getwSize())-1);
			int dp = this.nextToReplace(data);
			if(dp!=-1) {
				if(this.getMem()[data[1]][dp][1].contentEquals("1")) {
					System.out.println("cache fetch, 2 -- block transf. (MM > CM; CM > MM), 42+"+2*size);
					int acc = 2 + 2*(21 + size);
					System.out.println("Access Time: "+ acc+" cycles");
					CacheM.accesses++;
					CacheM.cycles=CacheM.cycles+acc;
				}else {
					System.out.println("cache fetch, 2 -- block transf. (MM>CM or CM>MM), 21+"+size);
					int acc = 2 + 21 + size;
					System.out.println("Access Time: "+ acc+" cycles");
					CacheM.accesses++;
					CacheM.cycles=CacheM.cycles+acc;
				}
			}else if(dp==-1){
			
			System.out.println("cache fetch, 2 -- block transf. (MM>CM or CM>MM), 21+"+size);
			int acc = 2 + 21 + size;
			System.out.println("Access Time: "+ acc+" cycles");
			CacheM.accesses++;
			CacheM.cycles=CacheM.cycles+acc;
			}
		}
	}
	
	public int nextToReplace(int [] data) {
		if(!this.nonBusyInSet(data[1])) {
			for(int i = 0;i<this.getlSize();i++) {
				if(this.getMem()[data[1]][i][3].contentEquals("0")) {
					return i;
				}
			}
		}return -1;
		
	}
	
	
	public double[] globalH(){
		double [] stats = new double [3];
		double rate = (CacheM.hits/CacheM.accesses);
		stats[0] = CacheM.hits;
		stats[1] = rate;
		stats[2] = CacheM.cycles;
		
		return stats;
	}
	
	}

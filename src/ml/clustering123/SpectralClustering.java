package ml.clustering123;
import static ml.manifold.Manifold.adjacencyDirected;
import static ml.utils.Matlab.diag;
import static ml.utils.Matlab.dotDivide;
import static ml.utils.Matlab.eigs;
import static ml.utils.Matlab.find;
import static ml.utils.Matlab.full;
import static ml.utils.Matlab.max;
import static ml.utils.Matlab.size;
import static ml.utils.Matlab.speye;
import static ml.utils.Matlab.sqrt;
import static ml.utils.Matlab.sum;
import static ml.utils.Printer.display;
import static ml.utils.Time.tic;
import static ml.utils.Time.toc;
import la.matrix.Matrix;
import la.matrix.SparseMatrix;
import la.vector.Vector;
import ml.clustering.Clustering;
import ml.clustering.KMeans;
import ml.options.ClusteringOptions;
import ml.options.KMeansOptions;
import ml.options.SpectralClusteringOptions;
import ml.utils.FindResult;
import ml.utils.Matlab;

/***
 * A Java implementation for spectral clustering.
 * 
 * @author Mingjie Qian
 * @version 1.0 Feb. 1st, 2014
 */
public class SpectralClustering extends Clustering {

	public SpectralClusteringOptions options;
	
	public SpectralClustering() {
		super();
		options = new SpectralClusteringOptions();
	}

	public SpectralClustering(int nClus) {
		super(nClus);
		options = new SpectralClusteringOptions(nClus);
	}

	public SpectralClustering(ClusteringOptions options) {
		super(options);
		this.options = new SpectralClusteringOptions(options);
	}
	
	public SpectralClustering(SpectralClusteringOptions options) {
		this.options = options;
	}
	
	
	/**
	 * For spectral clustering, we don't need initialization in the
	 * current implementation.
	 */
	@Override
	public void initialize(Matrix G0) {
	}

	
	
	@Override	
	public void clustering() {
		double [][] Graph=ReadFile.Graph;
//		Matrix X = dataMatrix;
		String TYPE = options.graphType;
		//double PARAM = options.graphParam;
		//PARAM = Math.ceil(Math.log(size(X, 1)) + 1);
		//if (PARAM == size(X, 1))
		//	PARAM--;
		String DISTANCEFUNCTION = options.graphDistanceFunction;
//		Matrix A = adjacencyDirected(X, TYPE, PARAM, DISTANCEFUNCTION);
//		Matrix A = X.copy();
	
//		Vector Z = max(A, 2)[0];
//		double WEIGHTPARAM = options.graphWeightParam;
//		WEIGHTPARAM = sum(Z) / Z.getDim();
	    
//		A = max(A, A.transpose());
		
		// W could be viewed as a similarity matrix
//		Matrix W = A.copy();
		Matrix W = dataMatrix;
		
//		System.out.println("W : ");
//		display(full(W));

		// Disassemble the sparse matrix
		/*FindResult findResult = find(A);
		int[] A_i = findResult.rows;
		int[] A_j = findResult.cols;
		double[] A_v = findResult.vals;
		
		String WEIGHTTYPE = options.graphWeightType;
		
		if (WEIGHTTYPE.equals("distance")) {
			for (int i = 0; i < A_i.length; i++) {
				W.setEntry(A_i[i], A_j[i], A_v[i]);
			}
		} else if (WEIGHTTYPE.equals("inner")) {
			for (int i = 0; i < A_i.length; i++) {
				W.setEntry(A_i[i], A_j[i], 1 - A_v[i] / 2);
			}
		} else if (WEIGHTTYPE.equals("binary")) {
			for (int i = 0; i < A_i.length; i++) {
				W.setEntry(A_i[i], A_j[i], 1);
			}
		} else if (WEIGHTTYPE.equals("heat")) {
			double t = -2 ;//* WEIGHTPARAM * WEIGHTPARAM;
			for (int i = 0; i < A_i.length; i++) {
				W.setEntry(A_i[i], A_j[i], 
			               Math.exp(A_v[i] * A_v[i] / t));
			}
		} else {
			System.err.println("Unknown Weight Type.");
			System.exit(1);
		}
		System.out.println("W : ");
		display(full(W));*/
		
		// Construct L_sym
		Vector D = sum(W, 2);
		for(int i =0; i<ReadFile.ClusterUserSize; i++){
			if(D.get(i)==0){
				D.set(i, 0.00000000001);
			}
		}
//		System.out.println("D : ");
//		display(full(D));
		//Matrix DD=diag(D);
		Matrix Dsqrt = diag(dotDivide(1, sqrt(D)));
//		System.out.println("Dsqrt : ");
//		display(full(Dsqrt));
		
		//Matrix L_sym = speye(size(W, 1)).minus(Dsqrt.mtimes(W).mtimes(Dsqrt));
		
		Matrix L_sym = diag(D).minus(W);
		
//		FindResult findResult = find(L_sym);
//		int[] L_i = findResult.rows;
//		int[] L_j = findResult.cols;
//		double[] L_v = findResult.vals;
//		for (int i = 0; i < L_i.length; i++) {
//			for (int j=0; j<L_j.length; j++){
//				if(Graph[i][j]!=0){
//					L_sym.setEntry(L_i[i], L_j[j], -1);
//				}				
//			}
//			
//		}
		
		
		
//		System.out.println("L : ");
//		display(full(L_sym));
		
		L_sym = Dsqrt.mtimes(L_sym).mtimes(Dsqrt);
//		System.out.println("L : ");
//		display(full(L_sym));
		
		// System.out.println(L_sym.getEntry(34, 110));
		Matrix eigRes[] = eigs(L_sym, this.options.nClus, "sm");
		Matrix V = eigRes[0];// 
//		display(V);
		Matrix U = Dsqrt.mtimes(V);// display(U);
		
		KMeansOptions kMeansOptions = new KMeansOptions();
		kMeansOptions.nClus = this.options.nClus;
		kMeansOptions.maxIter = this.options.maxIter;
		kMeansOptions.verbose = this.options.verbose;
		
		KMeans KMeans = new KMeans(kMeansOptions);
		KMeans.feedData(U);
		KMeans.initialize(null);
		KMeans.clustering();
		this.indicatorMatrix = KMeans.getIndicatorMatrix();
		
		System.out.println("Spectral clustering complete.");
	
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		tic();
		
		int nClus = 2;
		boolean verbose = false;
		int maxIter = 100;
		String graphType = "nn";
		double graphParam = 6;
		String graphDistanceFunction = "euclidean";
		String graphWeightType = "distance";
		double graphWeightParam = 1;
		ClusteringOptions options = new SpectralClusteringOptions(
				nClus,
				verbose,
				maxIter,
				graphType,
				graphParam,
				graphDistanceFunction,
				graphWeightType,
				graphWeightParam);
		
		Clustering spectralClustering = new SpectralClustering(options);
		
		/*String dataMatrixFilePath = "CNN - DocTermCount.txt";
		Matrix X = Data.loadMatrix(dataMatrixFilePath);*/
		
		double[][] data = {
				{0, 5.3, 0.2, 1},
				{5.3, 0, 0.5, 0.4},
				{0.2, 0.5, 0, 3.2},
				{1,0.4,3.2,0},
			    };
		/*Matrix X = new DenseMatrix(data);
		X = X.transpose();*/
		
		spectralClustering.feedData(data);
		spectralClustering.clustering(null);
		display(full(spectralClustering.getIndicatorMatrix()));
		
		/*String labelFilePath = "GroundTruth.txt";
		Matrix groundTruth = loadMatrix(labelFilePath);
		getAccuracy(spectralClustering.indicatorMatrix, groundTruth);*/
		
		System.out.format("Elapsed time: %.3f seconds\n", toc());

	}
	
	
	
	
	
	
}
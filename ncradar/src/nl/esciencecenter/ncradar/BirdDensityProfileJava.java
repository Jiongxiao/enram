package nl.esciencecenter.ncradar;

public class BirdDensityProfileJava extends JNIMethodsVol2Bird {

    private final RadarScanJava reflectivity;
    private final RadarScanJava radialVelocity;
    private final ParameterValues parameterValues;
    private int[] texture;
    private int[] cellImage;
    private int[] clutterImage;
    private final int nRang;
    private final int nAzim;



    public BirdDensityProfileJava(RadarScanJava reflectivity, RadarScanJava radialVelocity) throws Exception {

        this.reflectivity = reflectivity;
        this.radialVelocity = radialVelocity;
        this.parameterValues = new ParameterValues();

        nAzim = reflectivity.getNumberOfAzimuthBins();
        nRang = reflectivity.getNumberOfRangeBins();
        cellImage = new int[nAzim * nRang];
        clutterImage = new int[nAzim * nRang];

        {
            int nAzim = radialVelocity.getNumberOfAzimuthBins();
            int nRang = radialVelocity.getNumberOfRangeBins();

            if (nAzim != this.nAzim) {
                throw new Exception("Number of azimuth bins differ.");
            }
            if (nRang != this.nRang) {
                throw new Exception("Number of range bins differ.");
            }
        }

    }



    public int analyzeCells(int nCells, int cmFlagInt, int verboseInt) throws Exception {

        int[] dbzImage = this.reflectivity.getScanDataRaw();
        int[] vradImage = this.radialVelocity.getScanDataRaw();
        int[] texImage = this.getTexture();
        int[] clutterImage = this.getClutterImage();
        int[] cellImage = this.getCellImage();
        int dbznRang = this.reflectivity.getNumberOfRangeBins();
        int dbznAzim = this.radialVelocity.getNumberOfAzimuthBins();
        float dbzElev = (float) this.reflectivity.getElevationAngle();
        float dbzValueScale = (float) this.reflectivity.getDataScale();
        float dbzValueOffset = (float) this.reflectivity.getDataOffset();
        float vradValueScale = (float) this.radialVelocity.getDataScale();
        float vradValueOffset = (float) this.radialVelocity.getDataOffset();
        float clutterValueScale = 1.0f;
        float clutterValueOffset = 0.0f;
        float texValueScale = (float) this.parameterValues.getSTDEVSCALE();
        float texValueOffset = 0;

        
        int areaMin = this.parameterValues.getAREACELL();
        float cellDbzMin = (float) this.parameterValues.getDBZCELL();
        float cellStdDevMax = (float) this.parameterValues.getSTDEVCELL();
        float cellClutterFraction = (float) this.parameterValues.getCLUTPERCCELL();
        float vradMinValue = (float) parameterValues.getVRADMIN();
        float clutterValueMax = (float) this.parameterValues.getDBZCLUTTER();
        
        
        int nCellsValid = analyzeCells(dbzImage, vradImage, texImage,
                clutterImage, cellImage, dbznRang, dbznAzim,
                dbzElev, dbzValueScale, dbzValueOffset,
                vradValueScale, vradValueOffset,
                clutterValueScale, clutterValueOffset,
                texValueScale, texValueOffset,
                nCells, areaMin, cellDbzMin, cellStdDevMax,
                cellClutterFraction, vradMinValue, clutterValueMax,
                cmFlagInt, verboseInt);
        
        
        this.cellImage = cellImage.clone();
        
        int iGlobal;
        int nGlobal = cellImage.length;

        int minValue = cellImage[0];
        int maxValue = cellImage[0];
        for (iGlobal = 1;iGlobal < nGlobal;iGlobal++) {
            if (cellImage[iGlobal] < minValue) {
                minValue = cellImage[iGlobal];
            }
            if (cellImage[iGlobal] > maxValue) {
                maxValue = cellImage[iGlobal];
            }
        }
        System.out.println("minimum value in cellImage array = " + minValue);
        System.out.println("maximum value in cellImage array = " + maxValue);
        
        
        return nCellsValid; 
        
    }
    
    
    
    

    public void calcTexture() {

        int[] dbzImage = reflectivity.getScanDataRaw();
        int[] vradImage = radialVelocity.getScanDataRaw();
        int[] texImage = new int[nAzim * nRang];

        float dbzOffset = (float) reflectivity.getDataOffset();
        float dbzScale = (float) reflectivity.getDataScale();
        float vradOffset = (float) radialVelocity.getDataOffset();
        float vradScale = (float) radialVelocity.getDataScale();
        float texOffset = 0;
        float texScale = (float) this.parameterValues.getSTDEVSCALE();
        
        int nRangNeighborhood = this.parameterValues.getNTEXBINRANG();
        int nAzimNeighborhood = this.parameterValues.getNTEXBINAZIM();
        int nCountMin = this.parameterValues.getNTEXMIN();
        int vradMissing = this.radialVelocity.getMissingValueValue();
        
        calcTexture(texImage, dbzImage, vradImage,
                nRangNeighborhood, nAzimNeighborhood, nCountMin,
                texOffset, texScale,
                dbzOffset, dbzScale,
                vradOffset, vradScale, vradMissing,
                nRang, nAzim);

        texture = texImage.clone();

    }



    public int findCells() {

        int[] dbzImage = reflectivity.getScanDataRaw();
        int dbzMissing = reflectivity.getMissingValueValue();
        int dbznAzim = reflectivity.getNumberOfAzimuthBins();
        int dbznRang = reflectivity.getNumberOfRangeBins();
        float dbzRangeScale = (float) reflectivity.getRangeScale();
        float dbzValueOffset = (float) reflectivity.getDataOffset();
        float dbzValueScale = (float) reflectivity.getDataScale();
        float dbzThresMin = (float) this.parameterValues.getDBZMIN();
        // FIXME plus 5? magic number
        int rCellMax = (int) (this.parameterValues.getRANGMAX() + 5);
        
        int nCells = findCells(dbzImage, cellImage, dbzMissing, dbznAzim, dbznRang, dbzValueOffset, 
                dbzRangeScale, dbzValueScale, dbzThresMin, rCellMax);

        return nCells;
    };

    
    
    
    
    public void fringeCells() throws Exception {

        
        int nRang = this.reflectivity.getNumberOfRangeBins();
        int nAzim = this.reflectivity.getNumberOfAzimuthBins();
        float azimuthScale = (float) this.reflectivity.getAzimuthScaleDeg();
        float rangeScale = (float) this.reflectivity.getRangeScale();
        float fringeDist = (float) this.parameterValues.getEMASKMAX();
        
        fringeCells(cellImage, nRang, nAzim, azimuthScale, rangeScale, fringeDist);

    }
    

    


    public int[] getTexture() throws Exception {

        if (texture == null) {
            throw new Exception("The texture array hasn't been calculated yet.");
        }

        return texture.clone();
    };

    
    
    public int[] getCellImage() throws Exception {

        if (cellImage == null) {
            throw new Exception("The cellImage array hasn't been calculated yet.");
        }

        return cellImage.clone();
    };
    


    protected CellProperties sortCells(CellProperties cellPropIn) throws Exception {

        int[] iRangOfMax = cellPropIn.getAlliRangOfMax();
        int[] iAzimOfMax = cellPropIn.getAlliAzimOfMax();
        float[] dbzAvg = cellPropIn.getAllDbzAvg();
        float[] texAvg = cellPropIn.getAllTexAvg();
        float[] cv = cellPropIn.getAllCv();
        float[] area = cellPropIn.getAllArea();
        float[] clutterArea = cellPropIn.getAllClutterArea();
        float[] dbzMax = cellPropIn.getAllDbzMax();
        int[] index = cellPropIn.getAllIndex();
        char[] drop = cellPropIn.getAllDrop();

        int nCells = cellPropIn.getnCells();

        sortCells(iRangOfMax, iAzimOfMax, dbzAvg, texAvg, cv, area, clutterArea, dbzMax, index, drop, nCells);

        CellProperties cellPropOut = cellPropIn.clone();

        cellPropOut.copyCellPropertiesFrom(iRangOfMax, iAzimOfMax, dbzAvg, texAvg, cv, area, clutterArea, dbzMax, index, drop);

        return cellPropOut;

    }



    protected int[] updateMap(CellProperties cellProp, int nCells, int nGlobal, int minCellArea) throws Exception {

        int[] iRangOfMax = new int[nCells];
        int[] iAzimOfMax = new int[nCells];
        float[] dbzAvg = new float[nCells];
        float[] texAvg = new float[nCells];
        float[] cv = new float[nCells];
        float[] area = new float[nCells];
        float[] clutterArea = new float[nCells];
        float[] dbzMax = new float[nCells];
        int[] index = new int[nCells];
        char[] drop = new char[nCells];

        cellProp.copyCellPropertiesTo(iRangOfMax, iAzimOfMax, dbzAvg, texAvg, cv, area, clutterArea, dbzMax, index, drop);

        int nCellsValid;

        int[] cellImage = new int[nGlobal];

        nCellsValid = updateMap(cellImage, iRangOfMax, iAzimOfMax, dbzAvg, texAvg, cv, area, clutterArea, dbzMax, index, drop, nCells, nGlobal, minCellArea);

        cellProp.copyCellPropertiesFrom(iRangOfMax, iAzimOfMax, dbzAvg, texAvg, cv, area, clutterArea, dbzMax, index, drop);

        return cellImage;

    }



    public int getNumberOfAzimuthBins() {

        return this.reflectivity.getNumberOfAzimuthBins();
    }



    public int getNumberOfRangeBins() {

        return this.reflectivity.getNumberOfRangeBins();
    }



    public int[] getReflectivityRaw() {

        return this.reflectivity.getScanDataRaw();

    }



    public int[] getRadialVelocityRaw() {

        return this.radialVelocity.getScanDataRaw();

    }



    public int[] getClutterImage() {

        return clutterImage;
    }


}

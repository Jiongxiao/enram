//
// Copyright 2013 Netherlands eScience Center
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



// ****************************************************************************
// Definition of standard parameters.
// ****************************************************************************

#define DEG2RAD    (0.017453293)  // Degrees to radians.
#define RAD2DEG    (57.29578)     // Radians to degrees.

// ****************************************************************************
// Definition of general macros:
// ****************************************************************************

#define XABS(x)    (((x)<0)?(-(x)):(x))
#define SIGN(x)    (((x)<0)?-1:1)
#define ROUND(x)   (((x)>0)?(int)((x)+0.5):(int)((x)-0.5))
#define SQUARE(x)  ((x)*(x))
#define XYMAX(x,y) (((x)<(y))?(y):(x))
#define XYMIN(x,y) (((x)<(y))?(x):(y))

// ****************************************************************************
// Defined (default) parameters.
// ****************************************************************************


#define NEIGHBOURS  (5)      // Minimum number of directly neighouring pixels
                             // with dBZ>DBZRAIN [1-8]
#define PI          (3.14159265358979323846)
#define TRUE        ((int) (1))
#define FALSE       ((int) (0))


// ****************************************************************************
//  Structure for containing SCAN metadata:
// ****************************************************************************


struct cellprop {
    int iRangOfMax;
    int iAzimOfMax;
    float dbzAvg;
    float texAvg;
    float cv;
    int area;
    int clutterArea;
    float dbzMax;
    int index;
    int drop;
};

struct scanmeta {
    float heig;              // Height of radar antenna in km.
    float elev;              // Elevation of scan in deg.
    int nRang;               // Number of range bins in scan.
    int nAzim;               // Number of azimuth rays in scan.
    float rangeScale;        // Size of range bins in scan in km.
    float azimScale;         // Size of azimuth steps in scan in deg.
    float valueOffset;       // Offset value of quantity contained by scan.
    float valueScale;        // Scale of value of quantity contained by scan.
    unsigned char missing;   // Missing value of quantity contained by scan.
};


typedef struct cellprop CELLPROP;
typedef struct scanmeta SCANMETA;

// *****************************************************************************
// Function prototypes
// *****************************************************************************

int analyzeCells(const unsigned char *dbzImage, const unsigned char *vradImage,
        const unsigned char *texImage, const unsigned char *clutterImage, int *cellImage,
        const SCANMETA *dbzMeta, const SCANMETA *vradMeta, const SCANMETA *texMeta, const SCANMETA *clutterMeta,
        const int nCells, const int areaMin, const float cellDbzMin, const float cellStdDevMax, const float cellClutterFraction,
        const float absVradMin, const float clutterValueMax, const unsigned char cmFlag,
        const unsigned char verbose);

float calcDist(int range1, int azim1,int range2,int azim2,float rscale,float ascale);

void calcTexture(unsigned char *texImage, const unsigned char *vradImage, const unsigned char *dbzImage,
        const SCANMETA *texMeta, const SCANMETA *vradMeta, const SCANMETA *dbzMeta,
        const int nRangNeighborhood, const int nAzimNeighborhood,
        const int nCountMin);

void classifyGates(const SCANMETA dbzMeta, const SCANMETA vradMeta, const SCANMETA uzmeta, const SCANMETA clutterMeta,
        const int *cellImage, const unsigned char *dbzImage, const unsigned char *vradImage, unsigned char *uzscan, const unsigned char *clutterImage,
        float *zdata, int *nzdata, const float rangeMin, const float rangeMax, const float layerThickness, const float XOFFSET,
        const float XSCALE, const float XMEAN, const float heightOfInterest, const float azimMin, const float azimMax,
        const float absVradMin, const float dbzClutter, const float dbzMin, const float dBZx, const float DBZNOISE,
        const int iLayer, const unsigned char clutterFlag, const unsigned char uzflag, const unsigned char xflag);
        
void classifyGatesSimple(float* points, int iPoint, const float dbzMax, const float vradMin);

int detNumberOfGates(const int iLayer, const float layerThickness,
                     const float rangeMin, const float rangeMax,
                     const float rangeScale, const float elevAngle,
                     const int nRang, const int nAzim,
                     const float radarHeight);


int findCells(const unsigned char *dbzImage,
              int *cellImage,
              const SCANMETA *dbzMeta,
              const float dbzThresMin,
              const float rCellMax);

int findNearbyGateIndex(const int nAzimParent, const int nRangParent, const int iParent,
                        const int nAzimChild,  const int nRangChild,  const int iChild);

void fringeCells(int *cellImage,int nRang, int nAzim, float aScale, float rScale, float fringe);

int getListOfSelectedGates(const SCANMETA* vradMeta, const unsigned char *vradImage,
                           const SCANMETA* dbzMeta, const unsigned char *dbzImage,
                           const int *cellImage,
                           const float rangeMin, const float rangeMax,
                           const float altitudeMin, const float altitudeMax,
                           float* points, int iPoint);

int hasAzimuthGap(const float *points, const int nDims, const int nPoints, const int nBinsGap, const int nObsGapMin);

int includeGate(int iProfileType, int gateCode);

char* printGateCode(int gateCode);

void sortCells(CELLPROP *cellProp, int nCells);

void updateFlagFieldsInPointsArray(const float* yObs, const float* yFitted, const int* includedIndex, 
                                   const int nPointsIncluded, const float absVDifMax, float* points);

int updateMap(int *cellImage, const int nGlobal, CELLPROP *cellProp, const int nCells, const int minCellArea);

            





int detSvdfitArraySize(PolarVolume_t* volume, int* indexFrom, int* indexTo, 
    int nLayers, float layerThickness, float rangeMin, float rangeMax);

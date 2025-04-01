package utils;

public class GCJ02_WGS84 {
    static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    static double pi = 3.1415926535897932384626; //π
    static double a = 6378245.0; // 长半轴
    static double ee = 0.00669342162296594323; // 扁率

    public  static double[] wgs84togcj02(double lng, double lat) {
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 *pi;
        double magic = Math.sin(radlat);
        magic = 1 -ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 -ee)) / (magic * sqrtmagic) *pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) *pi);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new double[]{mglng, mglat};
    }


    public static double[] gcj02toWgs84(double lng,double lat) {
        // GCJ02(火星坐标系) 转GPS84: param lng: 火星坐标系的经度: param lat: 火星坐标系纬度: return :
        double dlat =transformlat(lng - 105.0, lat - 35.0);
        double dlng =transformlng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 *pi;
        double magic = Math.sin(radlat);
        magic = 1 -ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 -ee)) / (magic * sqrtmagic) *pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) *pi);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new double[]{lng * 2 - mglng, lat * 2 - mglat};
    }

    static double transformlat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * pi) + 20.0 * Math.sin(2.0 * lng * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * pi) + 40.0 * Math.sin(lat / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * pi) + 320 * Math.sin(lat * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    static double transformlng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng *pi) + 20.0 *
                Math.sin(2.0 * lng *pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng *pi) + 40.0 *
                Math.sin(lng / 3.0 *pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 *pi) + 300.0 *
                Math.sin(lng / 30.0 *pi)) * 2.0 / 3.0;
        return ret;
    }

}


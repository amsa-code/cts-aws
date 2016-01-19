package au.gov.amsa.cts2;

import com.github.davidmoten.geo.GeoHash;

public class Main {

    public static void main(String[] args) {

        System.out.println(GeoHash.coverBoundingBox(-20, 135, -50, 150));
        System.out.println(GeoHash.encodeHash(-42, 135));

    }

}

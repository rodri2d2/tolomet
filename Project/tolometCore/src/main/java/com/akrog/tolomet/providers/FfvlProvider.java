package com.akrog.tolomet.providers;

import com.akrog.tolomet.Station;
import com.akrog.tolomet.io.Downloader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by gorka on 18/07/16.
 */
public class FfvlProvider extends BaseProvider {
    public FfvlProvider() {
        super(REFRESH);
    }

    @Override
    public String getInfoUrl(String code) {
        return "http://www.balisemeteo.com/balise.php?idBalise="+code;
    }

    @Override
    public String getUserUrl(String code) {
        return "http://www.balisemeteo.com/balise_histo.php?interval=1&marks=true&idBalise="+code;
    }

    @Override
    public void configureDownload(Downloader downloader, Station station) {
        downloader.setUrl("http://data.ffvl.fr/xml/4D6F626942616C69736573/meteo/relevemeteo.xml.gz");
        downloader.setGzipped(true);
    }

    @Override
    public boolean configureDownload(Downloader downloader, Station station, long date) {
        return false;
    }

    @Override
    public void updateStation(Station station, String data) {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            df.setTimeZone(TIME_ZONE);

            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(data));

            int event = parser.getEventType();
            Long stamp = null;
            String text = null;
            boolean found = false;
            while( event != XmlPullParser.END_DOCUMENT ) {
                String tag = parser.getName();
                switch( event ) {
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if( tag.equals("idbalise") && text.equals(station.getCode()) ) {
                            text = null; found = true; break;
                        }
                        if( found && tag.equals("releve") )
                            return;
                        if( !found || text == null )
                            break;
                        if( tag.equals("date") )
                            stamp = df.parse(text).getTime();
                        else if( tag.equals("directVentMoy") )
                            station.getMeteo().getWindDirection().put(stamp,Float.parseFloat(text));
                        else if( tag.equals("vitesseVentMax") )
                            station.getMeteo().getWindSpeedMax().put(stamp, Float.parseFloat(text));
                        else if( tag.equals("vitesseVentMoy") )
                            station.getMeteo().getWindSpeedMed().put(stamp, Float.parseFloat(text));
                        else if( tag.equals("temperature") )
                            station.getMeteo().getAirTemperature().put(stamp, Float.parseFloat(text));
                        else if( tag.equals("pression") )
                            station.getMeteo().getAirPressure().put(stamp, Float.parseFloat(text));
                        text = null;
                        break;
                }
                event = parser.next();
            }
        } catch (XmlPullParserException | IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static final int REFRESH = 20;
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("CET");
}

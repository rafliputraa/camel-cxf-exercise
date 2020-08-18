package org.mycompany.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class PossiblyCompressedInboundInterceptor extends AbstractPhaseInterceptor<Message>  {

    private static final byte[] GZIP_SIGNATURE = new byte[] {(byte) 0x1f,(byte) 0x8b}; // GZip compressed files/streams will start with these two bytes (and importantly any regular XML payload will not)

    public PossiblyCompressedInboundInterceptor() {
        super(Phase.RECEIVE);
    }

    public void handleMessage(Message msg) throws Fault {
        // In order to handle a response (incoming) message that may or may not have been compressed using gzip we need to read the first 2 characters of the stream - see https://stackoverflow.com/questions/4818468/how-to-check-if-inputstream-is-gzipped

        PushbackInputStream pb = new PushbackInputStream( msg.getContent(InputStream.class), 2);
        byte [] signature = new byte[2];

        try {
	    int len = pb.read( signature ); //read the signature
            pb.unread( signature, 0, len ); //push back the signature to the stream
            if( Arrays.equals(signature,GZIP_SIGNATURE) ) { //check if matches standard gzip magic number
                GZIPInputStream gis = new GZIPInputStream( pb );
                msg.setContent(InputStream.class, gis);
            } else {
                msg.setContent(InputStream.class, pb);
            }
        } catch (IOException e) {
            // TODO: you should handle this exception too
        }
    }
}
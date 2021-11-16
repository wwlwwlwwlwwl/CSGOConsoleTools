/*
 * 11/19/04		1.0 moved to LGPL.
 * 12/12/99		Initial version.	mdm@techie.com
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package javazoom.jl.decoder;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * The JavaLayerUtils class is not strictly part of the JavaLayer API.
 * It serves to provide useful methods and system-wide hooks.
 * 
 * @author MDM
 */
public class JavaLayerUtils
{
	static private JavaLayerHook	hook = null;
	
	/**
	 * Deserializes the object contained in the given input stream.
	 * @param in	The input stream to deserialize an object from.
	 * @param cls	The expected class of the deserialized object. 
	 */
	static public Object deserialize(InputStream in, Class cls)
		throws IOException
	{
		if (cls==null)
			throw new NullPointerException("cls");
		
		Object obj = deserialize(in, cls);
		if (!cls.isInstance(obj))
		{
			throw new InvalidObjectException("type of deserialized instance not of required class.");
		}
		
		return obj;
	}
	
	/**
	 * Deserializes an object from the given <code>InputStream</code>.
	 * The deserialization is delegated to an <code>
	 * ObjectInputStream</code> instance. 
	 * 
	 * @param in	The <code>InputStream</code> to deserialize an object
	 *				from.
	 * 
	 * @return The object deserialized from the stream. 
	 * @exception IOException is thrown if there was a problem reading
	 *		the underlying stream, or an object could not be deserialized
	 *		from the stream.
	 * 
	 * @see java.io.ObjectInputStream
	 */
	static public Object deserialize(InputStream in)
		throws IOException
	{
		if (in==null)
			throw new NullPointerException("in");
		
		ObjectInputStream objIn = new ObjectInputStream(in);
		
		Object obj;
		
		try
		{
			obj = objIn.readObject();
		}
		catch (ClassNotFoundException ex)
		{
			throw new InvalidClassException(ex.toString());
		}
		
		return obj;
	}

	/**
	 * Deserializes an array from a given <code>InputStream</code>.
	 * 
	 * @param in		The <code>InputStream</code> to 
	 *					deserialize an object from.
	 *				
	 * @param elemType	The class denoting the type of the array
	 *					elements.
	 * @param length	The expected length of the array, or -1 if
	 *					any length is expected.
	 */
	static public Object deserializeArray(InputStream in, Class elemType, int length)
		throws IOException
	{
		if (elemType==null)
			throw new NullPointerException("elemType");
		
		if (length<-1)
			throw new IllegalArgumentException("length");
		
		Object obj = deserialize(in);
		
		Class cls = obj.getClass();
		
		
		if (!cls.isArray())
			throw new InvalidObjectException("object is not an array");
		
		Class arrayElemType = cls.getComponentType();
		if (arrayElemType!=elemType)
			throw new InvalidObjectException("unexpected array component type");
				
		if (length != -1)
		{
			int arrayLength = Array.getLength(obj);
			if (arrayLength!=length)
				throw new InvalidObjectException("array length mismatch");
		}
		
		return obj;
	}

	static public Object deserializeArrayResource(String name, Class elemType, int length)
		throws IOException
	{
		if (name.equals("sfd.ser")) {
			//sfd.ser -> hex -> byte -> stream
			byte[] magic = {-84, -19, 0, 5, 117, 114, 0, 2, 91, 70, 11, -100, -127, -119, 34, -32, 12, 66, 2, 0, 0, 120, 112, 0, 0, 2, 0, 0, 0, 0, 0, -71, -24, 0, 4, 59, 85, 0, 0, -69, -27, -128, 0, 60, -2, -96, 0, -67, -95, 8, 0, 61, -51, 112, 0, -65, 18, 113, 0, 63, -110, -113, 0, 63, 18, 113, 0, 61, -51, 112, 0, 61, -95, 8, 0, 60, -2, -96, 0, 59, -27, -128, 0, 59, 85, 0, 0, 57, -24, 0, 4, -73, -128, 0, 116, -71, -9, -1, -16, 59, 90, 0, 0, -68, 1, -64, 1, 60, -6, 0, 0, -67, -84, 104, 0, 61, -70, 56, 0, -65, 25, -88, 0, 63, -110, 120, 0, 63, 11, 56, 0, 61, -34, -16, 0, 61, -107, -96, 0, 61, 0, -16, 0, 59, -56, 127, -1, 59, 79, -1, -1, 57, -48, 0, 17, -73, -128, 0, 116, -70, 12, 0, 7, 59, 93, -1, -1, -68, 17, 64, 0, 60, -12, 0, 0, -67, -73, -72, 0, 61, -91, 64, 0, -65, 32, -40, 0, 63, -110, 52, 0, 63, 3, -1, 0, 61, -18, -64, 0, 61, -118, 72, 0, 61, 2, 0, 0, 59, -83, -128, 0, 59, 73, -1, -2, 57, -64, 0, 2, -73, -128, 0, 116, -70, 24, 0, 0, 59, 97, 0, 2, -68, 33, 64, 0, 60, -20, -96, 0, -67, -62, -24, 0, 61, -114, -120, 0, -65, 39, -2, 0, 63, -111, -61, 0, 62, -7, -106, 0, 61, -4, -32, 0, 61, 125, -16, 0, 61, 2, 112, 0, 59, -109, 0, 0, 59, 68, 0, 1, 57, -88, 0, 15, -73, -128, 0, 116, -70, 35, -1, -6, 59, 98, -1, -1, -68, 49, -64, 0, 60, -29, -64, 0, -67, -51, -24, 0, 61, 108, 0, 0, -65, 47, 21, 0, 63, -111, 36, -128, 62, -21, 64, 0, 62, 4, -80, 0, 61, 103, -112, 0, 61, 2, 80, 0, 59, 116, 0, 2, 59, 62, 0, 0, 57, -104, 0, 0, -73, -128, 0, 116, -70, 52, 0, 8, 59, 100, 0, 0, -68, 66, -64, 0, 60, -39, 96, 0, -67, -40, -72, 0, 61, 55, 112, 0, -65, 54, 25, 0, 63, -112, 90, 0, 62, -35, 2, 0, 62, 10, 32, 0, 61, 81, 112, 0, 61, 1, -80, 0, 59, 68, -1, -2, 59, 54, -1, -2, 57, -121, -1, -14, -73, -128, 0, 116, -70, 68, 0, 6, 59, 100, 0, 0, -68, 84, 0, 0, 60, -51, -128, 0, -67, -29, 56, 0, 60, -2, -96, 0, -65, 61, 6, 0, 63, -113, 99, -128, 62, -50, -28, 0, 62, 14, -52, 0, 61, 59, -64, 0, 61, 0, -112, 0, 59, 25, 0, 1, 59, 48, 0, 1, 57, -128, 0, 13, -72, 0, 0, 116, -70, 84, 0, 3, 59, 98, -1, -1, -68, 101, -64, 0, 60, -65, -32, 0, -67, -19, 104, 0, 60, -121, 64, 0, -65, 67, -39, 0, 63, -114, 65, -128, 62, -64, -20, 0, 62, 18, -76, 0, 61, 38, 112, 0, 60, -2, 0, 0, 58, -34, 0, 4, 59, 40, -1, -2, 57, 95, -1, -3, -72, 0, 0, 116, -70, 104, 0, 4, 59, 96, 0, 1, -68, 119, -64, 0, 60, -80, -64, 0, -67, -9, 48, 0, 58, -117, -1, -2, -65, 74, -115, 0, 63, -116, -12, -128, 62, -77, 34, 0, 62, 21, -32, 0, 61, 17, -96, 0, 60, -6, 32, 0, 58, -112, 0, 2, 59, 33, 0, 0, 57, 79, -1, -18, -72, 0, 0, 116, -70, 124, 0, 5, 59, 92, -1, -2, -68, -123, 0, 0, 60, -96, 0, 0, -66, 0, 68, 0, -68, 121, 127, -1, -65, 81, 30, 0, 63, -117, 126, 0, 62, -91, -118, 0, 62, 24, 88, 0, 60, -6, -64, 0, 60, -11, 64, 0, 58, 15, -1, -7, 59, 26, 0, 2, 57, 48, 0, 22, -72, 0, 0, 116, -70, -120, 0, 3, 59, 87, 0, 2, -68, -114, 32, 0, 60, -115, 96, 0, -66, 4, -84, 0, -67, 4, -96, 0, -65, 87, -118, 0, 63, -119, -33, 0, 62, -104, 44, 0, 62, 26, 28, 0, 60, -45, -128, 0, 60, -17, -32, 0, 56, 0, 0, 116, 59, 19, 0, 0, 57, 32, 0, 8, -72, 63, -1, -101, -70, -110, 0, 3, 59, 79, -1, -1, -68, -105, 64, 0, 60, 114, -128, 0, -66, 8, -52, 0, -67, 78, 64, 0, -65, 93, -54, 0, 63, -120, 23, -128, 62, -117, 14, 0, 62, 27, 60, 0, 60, -83, -128, 0, 60, -23, -64, 0, -71, -24, 0, 4, 59, 11, 0, 1, 57, 15, -1, -7, -72, 63, -1, -101, -70, -99, -1, -3, 59, 72, 0, 1, -68, -96, 96, 0, 60, 70, -128, 1, -66, 12, -104, 0, -67, -115, -88, 0, -65, 99, -35, 0, 63, -122, 42, 0, 62, 124, 108, 0, 62, 27, -72, 0, 60, -120, -32, 0, 60, -29, 32, 0, -70, 100, 0, 0, 59, 3, -1, -1, 56, -1, -1, -43, -72, 127, -1, -43, -70, -87, -1, -1, 59, 60, -1, -1, -68, -87, -128, 0, 60, 23, 64, 0, -66, 16, 12, 0, -67, -75, -48, 0, -65, 105, -66, 0, 63, -124, 22, -128, 62, 99, 80, 0, 62, 27, -100, 0, 60, 75, -128, 0, 60, -37, -32, 0, -70, -91, -1, -4, 58, -6, 0, 3, 56, -32, 0, 65, -72, 127, -1, -43, -70, -74, 0, 2, 59, 49, 0, 1, -68, -78, -128, 0, 59, -55, 0, 0, -66, 19, 32, 0, -67, -33, -112, 0, -65, 111, 105, 0, 63, -127, -33, 0, 62, 74, -48, 0, 62, 26, -16, 0, 60, 8, 64, 0, 60, -44, 64, 0, -70, -44, 0, 3, 58, -23, -1, -3, 56, -32, 0, 65, -72, -96, 0, 8, -70, -62, 0, 4, 59, 35, 0, 2, -68, -69, 64, 0, 59, 57, 0, 0, -66, 21, -60, 0, -66, 5, 112, 0, -65, 116, -36, 0, 63, 127, 10, 0, 62, 50, -4, 0, 62, 25, -72, 0, 59, -113, -1, -1, 60, -52, 64, 0, -70, -3, -1, -2, 58, -34, 0, 4, 56, -64, 0, 37, -72, -96, 0, 8, -70, -49, -1, -1, 59, 17, -1, -1, -68, -61, -32, 0, -70, 52, 0, 8, -66, 23, -4, 0, -66, 27, -36, 0, -65, 122, 19, 0, 63, 122, 19, 0, 62, 27, -36, 0, 62, 23, -4, 0, 58, 52, 0, 8, 60, -61, -32, 0, -69, 17, -1, -1, 58, -49, -1, -1, 56, -96, 0, 8, -72, -64, 0, 37, -70, -34, 0, 4, 58, -3, -1, -2, -68, -52, 64, 0, -69, -113, -1, -1, -66, 25, -72, 0, -66, 50, -4, 0, -65, 127, 10, 0, 63, 116, -36, 0, 62, 5, 112, 0, 62, 21, -60, 0, -69, 57, 0, 0, 60, -69, 64, 0, -69, 35, 0, 2, 58, -62, 0, 4, 56, -96, 0, 8, -72, -32, 0, 65, -70, -23, -1, -3, 58, -44, 0, 3, -68, -44, 64, 0, -68, 8, 64, 0, -66, 26, -16, 0, -66, 74, -48, 0, -65, -127, -33, 0, 63, 111, 105, 0, 61, -33, -112, 0, 62, 19, 32, 0, -69, -55, 0, 0, 60, -78, -128, 0, -69, 49, 0, 1, 58, -74, 0, 2, 56, 127, -1, -43, -72, -32, 0, 65, -70, -6, 0, 3, 58, -91, -1, -4, -68, -37, -32, 0, -68, 75, -128, 0, -66, 27, -100, 0, -66, 99, 80, 0, -65, -124, 22, -128, 63, 105, -66, 0, 61, -75, -48, 0, 62, 16, 12, 0, -68, 23, 64, 0, 60, -87, -128, 0, -69, 60, -1, -1, 58, -87, -1, -1, 56, 127, -1, -43, -72, -1, -1, -43, -69, 3, -1, -1, 58, 100, 0, 0, -68, -29, 32, 0, -68, -120, -32, 0, -66, 27, -72, 0, -66, 124, 108, 0, -65, -122, 42, 0, 63, 99, -35, 0, 61, -115, -88, 0, 62, 12, -104, 0, -68, 70, -128, 1, 60, -96, 96, 0, -69, 72, 0, 1, 58, -99, -1, -3, 56, 63, -1, -101, -71, 15, -1, -7, -69, 11, 0, 1, 57, -24, 0, 4, -68, -23, -64, 0, -68, -83, -128, 0, -66, 27, 60, 0, -66, -117, 14, 0, -65, -120, 23, -128, 63, 93, -54, 0, 61, 78, 64, 0, 62, 8, -52, 0, -68, 114, -128, 0, 60, -105, 64, 0, -69, 79, -1, -1, 58, -110, 0, 3, 56, 63, -1, -101, -71, 32, 0, 8, -69, 19, 0, 0, -72, 0, 0, 116, -68, -17, -32, 0, -68, -45, -128, 0, -66, 26, 28, 0, -66, -104, 44, 0, -65, -119, -33, 0, 63, 87, -118, 0, 61, 4, -96, 0, 62, 4, -84, 0, -68, -115, 96, 0, 60, -114, 32, 0, -69, 87, 0, 2, 58, -120, 0, 3, 56, 0, 0, 116, -71, 48, 0, 22, -69, 26, 0, 2, -70, 15, -1, -7, -68, -11, 64, 0, -68, -6, -64, 0, -66, 24, 88, 0, -66, -91, -118, 0, -65, -117, 126, 0, 63, 81, 30, 0, 60, 121, 127, -1, 62, 0, 68, 0, -68, -96, 0, 0, 60, -123, 0, 0, -69, 92, -1, -2, 58, 124, 0, 5, 56, 0, 0, 116, -71, 79, -1, -18, -69, 33, 0, 0, -70, -112, 0, 2, -68, -6, 32, 0, -67, 17, -96, 0, -66, 21, -32, 0, -66, -77, 34, 0, -65, -116, -12, -128, 63, 74, -115, 0, -70, -117, -1, -2, 61, -9, 48, 0, -68, -80, -64, 0, 60, 119, -64, 0, -69, 96, 0, 1, 58, 104, 0, 4, 56, 0, 0, 116, -71, 95, -1, -3, -69, 40, -1, -2, -70, -34, 0, 4, -68, -2, 0, 0, -67, 38, 112, 0, -66, 18, -76, 0, -66, -64, -20, 0, -65, -114, 65, -128, 63, 67, -39, 0, -68, -121, 64, 0, 61, -19, 104, 0, -68, -65, -32, 0, 60, 101, -64, 0, -69, 98, -1, -1, 58, 84, 0, 3, 56, 0, 0, 116, -71, -128, 0, 13, -69, 48, 0, 1, -69, 25, 0, 1, -67, 0, -112, 0, -67, 59, -64, 0, -66, 14, -52, 0, -66, -50, -28, 0, -65, -113, 99, -128, 63, 61, 6, 0, -68, -2, -96, 0, 61, -29, 56, 0, -68, -51, -128, 0, 60, 84, 0, 0, -69, 100, 0, 0, 58, 68, 0, 6, 55, -128, 0, 116, -71, -121, -1, -14, -69, 54, -1, -2, -69, 68, -1, -2, -67, 1, -80, 0, -67, 81, 112, 0, -66, 10, 32, 0, -66, -35, 2, 0, -65, -112, 90, 0, 63, 54, 25, 0, -67, 55, 112, 0, 61, -40, -72, 0, -68, -39, 96, 0, 60, 66, -64, 0, -69, 100, 0, 0, 58, 52, 0, 8, 55, -128, 0, 116, -71, -104, 0, 0, -69, 62, 0, 0, -69, 116, 0, 2, -67, 2, 80, 0, -67, 103, -112, 0, -66, 4, -80, 0, -66, -21, 64, 0, -65, -111, 36, -128, 63, 47, 21, 0, -67, 108, 0, 0, 61, -51, -24, 0, -68, -29, -64, 0, 60, 49, -64, 0, -69, 98, -1, -1, 58, 35, -1, -6, 55, -128, 0, 116, -71, -88, 0, 15, -69, 68, 0, 1, -69, -109, 0, 0, -67, 2, 112, 0, -67, 125, -16, 0, -67, -4, -32, 0, -66, -7, -106, 0, -65, -111, -61, 0, 63, 39, -2, 0, -67, -114, -120, 0, 61, -62, -24, 0, -68, -20, -96, 0, 60, 33, 64, 0, -69, 97, 0, 2, 58, 24, 0, 0, 55, -128, 0, 116, -71, -64, 0, 2, -69, 73, -1, -2, -69, -83, -128, 0, -67, 2, 0, 0, -67, -118, 72, 0, -67, -18, -64, 0, -65, 3, -1, 0, -65, -110, 52, 0, 63, 32, -40, 0, -67, -91, 64, 0, 61, -73, -72, 0, -68, -12, 0, 0, 60, 17, 64, 0, -69, 93, -1, -1, 58, 12, 0, 7, 55, -128, 0, 116, -71, -48, 0, 17, -69, 79, -1, -1, -69, -56, 127, -1, -67, 0, -16, 0, -67, -107, -96, 0, -67, -34, -16, 0, -65, 11, 56, 0, -65, -110, 120, 0, 63, 25, -88, 0, -67, -70, 56, 0, 61, -84, 104, 0, -68, -6, 0, 0, 60, 1, -64, 1, -69, 90, 0, 0, 57, -9, -1, -16, 55, -128, 0, 116};
			ByteArrayInputStream stream = new ByteArrayInputStream(magic);
			return deserializeArray(stream,elemType,length);
		}

		InputStream str = getResourceAsStream(name);
		if (str==null)
			throw new IOException("unable to load resource '"+name+"'");

		Object obj = deserializeArray(str, elemType, length);
		
		return obj;
	}	
	
	static public void serialize(OutputStream out, Object obj)
		throws IOException
	{
		if (out==null)
			throw new NullPointerException("out");
		
		if (obj==null)
			throw new NullPointerException("obj");
		
		ObjectOutputStream objOut = new ObjectOutputStream(out);
		objOut.writeObject(obj);
				
	}

	/**
	 * Sets the system-wide JavaLayer hook.
	 */
	static synchronized public void setHook(JavaLayerHook hook0)		
	{
		hook = hook0;
	}
	
	static synchronized public JavaLayerHook getHook()
	{
		return hook;	
	}
	
	/**
	 * Retrieves an InputStream for a named resource. 
	 * 
	 * @param name	The name of the resource. This must be a simple
	 *				name, and not a qualified package name.
	 * 
	 * @return		The InputStream for the named resource, or null if
	 *				the resource has not been found. If a hook has been 
	 *				provided, its getResourceAsStream() method is called
	 *				to retrieve the resource. 
	 */
	static synchronized public InputStream getResourceAsStream(String name)
	{
		InputStream is = null;
		
		if (hook!=null)
		{
			is = hook.getResourceAsStream(name);	
		}
		else
		{
			Class cls = JavaLayerUtils.class;
			is = cls.getResourceAsStream(name);
		}
		
		return is;		
	}
}

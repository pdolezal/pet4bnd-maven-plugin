package net.yetamine.pet4bnd.mojo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.yetamine.pet4bnd.model.Persistable;

/**
 * An editor for the version element in a POM file designed to preserve the file
 * content intact as much as possible.
 */
final class PomVersionEditor implements Persistable {

    /** Buffer for reading the content. */
    private static final int BUFFER_SIZE = 2048;

    /** Name of the version element. */
    private static final String VERSION_ELEMENT = "version";
    /** XML namespace of the version element to find. */
    private static final String VERSION_XMLNS = "http://maven.apache.org/POM/4.0.0";

    /** Original content. */
    private final String content;
    /** Charset of the input. */
    private final Charset charset;
    /** Starting offset of the version. */
    private final int versionOffset;
    /** Ending offset the version. */
    private final int versionEnding;
    /** Version string to store. */
    private String version;

    /**
     * Creates a new instance.
     *
     * @param source
     *            the source of the input. It must not be {@code null}.
     *
     * @throws IOException
     *             if the input could not be processed
     */
    public PomVersionEditor(Path source) throws IOException {
        this(Files.readAllBytes(source));
    }

    /**
     * Creates a new instance.
     *
     * @param source
     *            the source of the input. It must not be {@code null}.
     *
     * @throws IOException
     *             if the input could not be processed
     */
    public PomVersionEditor(InputStream source) throws IOException {
        this(readBytes(source));
    }

    /**
     * Creates a new instance.
     *
     * @param source
     *            the bytes of the input. It must not be {@code null}.
     *
     * @throws IOException
     *             if the input could not be processed
     */
    private PomVersionEditor(byte[] source) throws IOException {
        // Make the XML input processing factory to find more about the input
        final XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);

        // Find out the input encoding to be precise in finding the character offset
        final String encoding = readEncoding(factory, source);
        charset = (encoding != null) ? Charset.forName(encoding) : StandardCharsets.UTF_8;
        content = new String(source, charset);

        // Find the position of the version information
        int offset = -1;
        int ending = -1;

        try (Reader reader = new StringReader(content)) {
            final XMLStreamReader xml = factory.createXMLStreamReader(reader);

            try {
                int nesting = -1;           // Nesting: let root have level of zero
                boolean inside = false;     // Processing the right version element

                while (xml.hasNext()) {
                    final int type = xml.next();

                    // Possibly found the start of the element
                    if (type == XMLStreamConstants.START_ELEMENT) {
                        if ((nesting++ != 0) || inside || (0 <= offset)) {
                            continue;
                        }

                        // Found the correct version element, mark its occurrence
                        if (VERSION_ELEMENT.equals(xml.getLocalName()) && VERSION_XMLNS.equals(xml.getNamespaceURI())) {
                            offset = xml.getLocation().getCharacterOffset();
                            inside = true;
                        }

                        continue;
                    }

                    // Possibly found the end of the element
                    if (type == XMLStreamConstants.END_ELEMENT) {
                        if ((--nesting == 0) && inside) {
                            ending = xml.getLocation().getCharacterOffset();
                            break; // Done here
                        }
                    }
                }
            } finally {
                reader.close();
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }

        if (offset == -1) {
            throw new IOException("Could not locate the proper <version> element.");
        }

        assert (offset > 0);
        assert (ending > offset);
        // The ending mark includes the whole ending element, we have to strip it off
        versionEnding = content.lastIndexOf("<", ending);
        assert (offset <= versionEnding);
        assert (versionEnding <= ending);
        versionOffset = offset;
        version = content.substring(versionOffset, versionEnding);
    }

    /**
     * Sets the version string to store.
     *
     * @param value
     *            the version string. It must not be {@code null}.
     *
     * @return this instance
     */
    public PomVersionEditor version(String value) {
        version = Objects.requireNonNull(value);
        return this;
    }

    /**
     * Gets the version string retrieved from the input.
     *
     * @return the version string
     */
    public String version() {
        return version;
    }

    /**
     * @see net.yetamine.pet4bnd.model.Persistable#persist(java.io.OutputStream)
     */
    public void persist(OutputStream sink) throws IOException {
        try (Writer writer = new OutputStreamWriter(sink, charset)) {
            writer.append(content, 0, versionOffset);
            writer.append(version);
            writer.append(content, versionEnding, content.length());
        }
    }

    /**
     * Finds encoding of the given XML file if possible.
     *
     * @param factory
     *            the factory of the input reader. It must not be {@code null}.
     * @param bytes
     *            the bytes of the XML file. It must not be {@code null}.
     *
     * @return the encoding, or {@code null} if could not be retrieved
     *
     * @throws IOException
     *             if the input could not be processed
     */
    private static String readEncoding(XMLInputFactory factory, byte[] bytes) throws IOException {
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            final XMLStreamReader reader = factory.createXMLStreamReader(is);

            try {
                final String encoding = reader.getEncoding();
                return (encoding != null) ? encoding : reader.getCharacterEncodingScheme();
            } finally {
                reader.close();
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Reads the bytes of an input into an array.
     *
     * @param source
     *            the source stream. It must not be {@code null}.
     *
     * @return the byte array with the input content
     *
     * @throws IOException
     *             if the source could not be read
     */
    private static byte[] readBytes(InputStream source) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[BUFFER_SIZE];
            for (int read; ((read = source.read(buffer, 0, buffer.length)) != -1);) {
                os.write(buffer, 0, read);
            }

            return os.toByteArray();
        }
    }
}

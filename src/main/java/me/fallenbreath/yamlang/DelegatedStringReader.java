package me.fallenbreath.yamlang;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class DelegatedStringReader extends Reader
{
    private String contents;
    private Reader delegate;

    public DelegatedStringReader(Reader reader) throws IOException
    {
        this.contents = CharStreams.toString(reader);
        this.delegate = reader;
    }

    public String getContents()
    {
        return contents;
    }

    public void setContents(String contents)
    {
        this.contents = contents;
        this.delegate = new StringReader(contents);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        return this.delegate.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException
    {
        this.delegate.close();
    }
}
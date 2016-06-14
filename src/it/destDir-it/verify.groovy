File index = new File( (File)basedir, "target/site/myDest/index.html" );
File libFoo = new File( (File)basedir, "target/site/myDest/foo" );
File compBar = new File( (File)basedir, "target/site/myDest/foo/bar.html" );

assert index.isFile()       // the report index file
assert libFoo.isDirectory() // the directory for library 'foo'
assert compBar.isFile()     // the file for component 'bar'

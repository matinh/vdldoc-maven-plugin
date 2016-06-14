File index = new File( (File)basedir, "target/myOut/vdldoc/index.html" );
File libFoo = new File( (File)basedir, "target/myOut/vdldoc/foo" );
File compBar = new File( (File)basedir, "target/myOut/vdldoc/foo/bar.html" );

assert index.isFile()       // the report index file
assert libFoo.isDirectory() // the directory for library 'foo'
assert compBar.isFile()     // the file for component 'bar'

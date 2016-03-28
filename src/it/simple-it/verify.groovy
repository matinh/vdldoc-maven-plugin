File index = new File( basedir, "target/vdldoc/index.html" );
File libFoo = new File( basedir, "target/vdldoc/foo" );

assert index.isFile()
assert libFoo.isDirectory()

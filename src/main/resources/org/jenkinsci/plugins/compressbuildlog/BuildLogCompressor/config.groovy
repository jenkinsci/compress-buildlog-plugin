package org.jenkinsci.plugins.compressbuildlog.BuildLogCompressor

import org.jenkinsci.plugins.compressbuildlog.BuildLogCompressor

def f=namespace(lib.FormTagLib)

f.optionalBlock(title:_("Compress build log"), name:"buildlogcompression",
        checked:instance!=null, help:"/plugin/compress-buildlog/help.html") {
}
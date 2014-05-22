package org.jenkinsci.plugins.database.orientdb.LocalOrientDbDatabase

def f = namespace(lib.FormTagLib)

f.entry(field:"path",title:_("File Path")) {
	f.textbox()
}
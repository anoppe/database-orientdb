package org.jenkinsci.plugins.ymonitor.sentinel.view.GlobalDatabaseConsole

import com.orientechnologies.orient.core.record.impl.ODocument

def f = namespace(lib.FormTagLib)
def l = namespace(lib.LayoutTagLib)

// XXX require RUN_SCRIPTS
l.layout{
	l.main_panel {
		form(method:"post",action:"execute") {
			raw("""
<h2>SQL</h2>
<textarea name=sql style='width:100%; height:5em'></textarea>
            """)
			div {
				f.submit(value:"Execute")
			}
		}

		if (request.getAttribute("message")!=null) {
			p(message)
		}

		if (request.getAttribute("result")!=null) {
			// renders the result
			h2("Result")
			table(class: "bigtable") {
				tbody {
					for (ODocument document : result) {
						tr {
							td(document.toString())
						}
					}
				}
			}
		}
	}
}

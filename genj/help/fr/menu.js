
 document

 var count = 0;

 function link(title, page) {
  if (count++>0) {
   document.write("|");
  }
  if (window.location.href.indexOf(page)<0) {
   document.write("<a href="+page+">"+title+"</a>");
  } else {
   document.write(title);
  }
 }

 link("Introduction", "intro.html");
 link("Gedcom", "gedcom.html");
 link("Control Center", "cc.html");
 link("Views", "views.html");
 link("Context Menues", "context.html");
 link("Entities", "entities.html");
 link("Specials", "specials.html");
 link("Team", "team.html");

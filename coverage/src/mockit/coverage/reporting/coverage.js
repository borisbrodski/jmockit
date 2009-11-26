var cellShown;
var pathIdShown;
var lineIdsShown;

function hidePath(pathId)
{
   if (lineIdsShown) {
      for (var i = 0; i < lineIdsShown.length; i++) {
         var line = document.getElementById(lineIdsShown[i]);
         line.style.outlineStyle = 'none';
      }

      cellShown.style.outlineWidth = 'thin';
      cellShown = lineIdsShown = null; return pathId == pathIdShown;
   }

   return false;
 }

function showPath(cell, pathId, lineIdsStr)
{
   if (hidePath(pathId)) return;

   cellShown = cell;
   pathIdShown = pathId;
   lineIdsShown = lineIdsStr.split(' ');

   for (var i = 0; i < lineIdsShown.length; i++) {
      var line = document.getElementById(lineIdsShown[i]);
      line.style.outline = 'thin dashed #0000FF';
   }

   cell.style.outlineWidth = 'medium';
}

function showHide(callPoints)
{
   var list = callPoints.nextSibling.nextSibling.style;
   list.display = list.display == 'none' ? 'block' : 'none';
}

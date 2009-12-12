var cellShown;
var pathIdShown;
var lineIdsShown;

function hidePath(cell)
{
   if (lineIdsShown) {
      for (var i = 0; i < lineIdsShown.length; i++) {
         var line = document.getElementById(lineIdsShown[i]);
         line.style.outlineStyle = 'none';
      }

      lineIdsShown = null;
      cellShown.style.outlineWidth = 'thin';

      var sameCell = cell == cellShown;
      cellShown = null;
      return sameCell;
   }

   return false;
}

function showPath(cell, pathId, lineIdsStr)
{
   if (hidePath(cell)) return;

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

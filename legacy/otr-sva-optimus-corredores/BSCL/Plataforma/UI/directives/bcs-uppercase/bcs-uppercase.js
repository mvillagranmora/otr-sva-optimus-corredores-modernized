angular.module('ModuloPlataforma').directive('bcsUppercase', function($parse,$timeout){
    return {
        require: 'ngModel',
        link: function(scope, elem, attrs, controller){
        	
        	function doGetCaretPosition (oField) {

        		  // Initialize
        		  var iCaretPos = 0;

        		  // IE Support
        		  if (document.selection) {

        		    // Set focus on the element
        		    oField[0].focus();

        		    // To get cursor position, get empty selection range
        		    var oSel = document.selection.createRange();

        		    // Move selection start to 0 position
        		    oSel.moveStart('character', -oField[0].value.length);

        		    // The caret position is selection length
        		    iCaretPos = oSel.text.length;
        		  }

        		  // Firefox support
        		  else if (oField[0].selectionStart || oField[0].selectionStart == '0')
        		    iCaretPos = oField[0].selectionStart;

        		  // Return results
        		  return iCaretPos;
        		}
        	
        	
        	function setSelectionRange(input, selectionStart, selectionEnd) {
        		  if (input.setSelectionRange) {
        		    input.focus();
        		    input.setSelectionRange(selectionStart, selectionEnd);
        		  }
        		  else if (input.createTextRange) {
        		    var range = input.createTextRange();
        		    range.collapse(true);
        		    range.moveEnd('character', selectionEnd);
        		    range.moveStart('character', selectionStart);
        		    range.select();
        		  }
        		}

        		function setCaretToPos (input, pos) {
        		  setSelectionRange(input, pos, pos);
        		}
        	
        	var fnUpper = function(value){            	
        		var txt='';
        		var pos = doGetCaretPosition(elem)
        		if (value!==undefined && (typeof value == 'string' || typeof value == 'number')) {
                	var ucase = $parse(attrs.bcsUppercase);
					if (ucase(scope)==='N')  txt=value.toString();
					else txt=value.toString().toUpperCase();
        		}
                if (value!==txt) {
					controller.$setViewValue(txt);
              		controller.$render();
                }
                setCaretToPos(elem[0],pos,pos);
                return txt; 
            };                   		
            controller.$parsers.push(fnUpper); 
            $timeout(function() {
            	var modelGet = $parse(attrs.ngModel);
            	if (modelGet(scope)!==undefined) {
            		var modelSet = modelGet.assign;
            		if (modelSet!==undefined) modelSet(scope,fnUpper(modelGet(scope)));         
            	}
            });
        }
    };
});

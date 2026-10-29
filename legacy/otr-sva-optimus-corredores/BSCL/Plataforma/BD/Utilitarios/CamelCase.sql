SET SCHEMA "BSCL";
GO
DROP FUNCTION  "BSCL.Plataforma.BD.Utilitarios::CamelCase";
GO
CREATE FUNCTION
-- 
--  Proposito: Devuelve string el formato CamelCase
--  Autor: CST - BCS
"BSCL.Plataforma.BD.Utilitarios::CamelCase"(
	linea nvarchar(5000)
)
RETURNS result nvarchar(5000)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER AS
BEGIN
  declare _index integer;
  declare _text nvarchar(5000);
  declare _word nvarchar(5000);  
  
  declare _indexToken integer;
  declare _textToken nvarchar(5000);
  declare _wordToken nvarchar(5000);
  declare _delimToken char;
  
  _text:='';
  _index:=1;
  WHILE _index>0 DO
		  _delimToken:= ' ';
		  _textToken:=linea||:_delimToken;
		  _indexToken:=1;
		  _wordToken:='';
		  WHILE LOCATE(:_textToken,:_delimToken) > 0 DO    
		    if _indexToken = _index then
		      _wordToken:=SUBSTR_BEFORE(:_textToken,:_delimToken);
		    end if;  
		    _indexToken:=_indexToken+1;  
		    _textToken:=SUBSTR_AFTER(:_textToken,:_delimToken);  
		  END WHILE;
		  		 
    _word := _wordToken;
    IF (LENGTH(:_word)=0) THEN
      break;
    END IF;
    _word := lower(:_word);
    IF (LENGTH(:_word)>3) THEN
      _text := _text||' '||UPPER(LEFT(:_word,1))||SUBSTRING(:_word,2);
    ELSE
      _text := _text||' '||_word;
    END IF;    
    _index:=_index+1;  
  END WHILE;
   result:=_text;
END;
GO
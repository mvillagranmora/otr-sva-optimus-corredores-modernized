//Script que recibe parametros desde el servicio 601200 java GenerarCartolaPDF.java 
//Autor: Nelson Ramos
//Celula: ZORRO
//Bolsa de santiago 2019 

const puppeteer = require('puppeteer');
//parametros que recibe script al momento de ser ejecutado

const p_IdEmpresa = process.argv[2];
const p_IdUsuario = process.argv[3];
const p_IdModulo = process.argv[4];
const p_FechaDesde = process.argv[5];
const p_FechaHasta = process.argv[6];
const IdCliente = process.argv[7];
const IdPortafolio = process.argv[8];
const p_host = process.argv[9];
const ruta = process.argv[10];
const timestamp = process.argv[11];
const token = process.argv[12];







//calculo de fecha para ser impresa en el pdf generado

var today = new Date();
var date = today.getDate() + '-' + (today.getMonth() + 1) + '-' + today.getFullYear();

//funcion principal que instancia el navegador con los parametros recibidos, 
//emula un navegador con resolucion de 2000 x 1980, espera a que carguen los selectores y 
//renderiza la pagina para ser impresa en un pdf
//funcion final generar el pdf con margenes, resoluciones y el nombre de usuario

(async()=>{
    try {
    	    	
    	console.log("INIT generar cartola");
        const browser = await puppeteer.launch({"headless":true,args:['--no-sandbox','--start-maximized']});
        const page = await browser.newPage();
		const url = p_host+ruta+
		"?IdEmpresa="+p_IdEmpresa+
		"&p_IdUsuario="+p_IdUsuario+
		"&p_IdModulo="+p_IdModulo+
		"&p_FechaDesde="+p_FechaDesde+
		"&p_FechaHasta="+p_FechaHasta+
		"&p_IdCliente="+IdCliente+
		"&IdPortafolio="+IdPortafolio+
		"&token="+token;
        console.log("visitando: " + url);
        await page.goto(url);
        console.log("se abrio la URL, esperando elemento stop");
        await page.waitForSelector('#stop');
        console.log("se encontro el  #stop");
        await page.emulateMedia('print');
        await page.pdf({
        	margin:{
        		top:'10',right:'25',left:'25',bottom:'10'
			},
			width:'1700px',
			height:'1000px',
			path: "./"+IdCliente+"_"+timestamp+'.pdf',
			landscape:false,
			printBackground:true,
			preferCSSPageSize:true
		}).then(function(){
			console.log("se genero el pdf");
			browser.close();
			console.log("se cerro el browser");
			console.log("se termina el programa");
			process.exit(0);
		});
    } catch (error) {
        console.log("Error de ejecucion " + error);
        process.exit(1);
    }
}
)();
/////////////////////////////////////////////////////////////////////////////////////////////////////

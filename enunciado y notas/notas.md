tf = term frequency basicamente significa cuánto un término aparece en un archivo
apariciones de un término en un documento d/total de términos en el documento d
> intenta determinar el grado de importancia de una palabra a lo largo de un archivo
 
idf (t,D)
ln(Número de documentos en el corpus / número de documentos en el corpus que contengan al término t)
> intenta determinar el grado de importancia de una palabra a lo largo de todos los archivo

tipo Oracion
tokens list[String]
metodo relaiveFreq(token: String): Real
metodo contains(token: String): Boolean

SingletonObject 
val stopWords
tokenize(sentence: String): List[String]

@list[Oracion]
tipo Corpus/Calculator 
oraciones list[Oracion]
tokens list[String]

metodo getNumOfOraciones(): Int
// numero de oraciones en las que aparece el token
metodo appearsInOraciones(token: String): Int
metodo getAllTermsFreq(): map[String, Int]
metodo getCantOraciones(): Int
metodo getIDF(token: String): Real
metodo getTF(term: String): Int

metodo getTokensScores(): Map[(token) String, (score) Real]
metodo getSentencesScores(): Map[(sentence) String, (score) Real]

TF(termino, Oracion) es la cantidad de veces que t aparece en s dividido por el total de tokens en s


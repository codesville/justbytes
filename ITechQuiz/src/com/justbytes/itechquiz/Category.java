package com.justbytes.itechquiz;

public enum Category {
	Java,
	DotNet{
		public String toString(){
			return ".NET";
		}
	},
	Unix,
	Sql,
	Hibernate,
	Spring,
	SOA,
	XML,
	JavaScript
}

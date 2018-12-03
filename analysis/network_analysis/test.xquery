copy $test := doc("links_to_delete_with_metadata.xml")
modify ( 
for $l in $test
return delete node $l
)
return $test

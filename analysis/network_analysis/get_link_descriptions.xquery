<links_to_delete>{
let $raw_ids_doc := doc("link_ids.xml")
let $origininal_network := doc("original_network.xml")
let $raw_ids := fn:tokenize($raw_ids_doc/link_ids, "\n")

let $original_network := doc("original_network.xml")
for $link in $original_network/network/links/link
where $link/@id  = $raw_ids
return $link
}
</links_to_delete>
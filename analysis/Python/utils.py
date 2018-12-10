
def network_csv_to_geopandas(in_file, out_file):
    network = pd.read_csv(in_file)

    def to_lines(network):
        for i in range(len(network)):
            if i%10000==0:
                print(i/len(network))
            
            yield {
                'id': network.iloc[i].id,
                'length': network.iloc[i].length,
                'geometry': LineString([(network.iloc[i].fromX, network.iloc[i].fromY), (network.iloc[i].toX, network.iloc[i].toY)])
            }

    network_frame = gpd.GeoDataFrame.from_records(list(to_lines(network)))
    network_frame.crs = {'init': 'epsg:2154'}

    network_frame.to_file(out_file)
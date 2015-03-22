var Movie = React.createClass({
    render: function () {
        return (
            <div className="movie">
                {this.props.title}
            </div>
        );
    }
});

var MovieList = React.createClass({
    render: function () {
        var movies = this.props.data.map(function(movie) {
            return (<Movie title={movie.mTitle}>{movie.mTitle}</Movie>);
        });

        return (
            <div>
                {movies}
            </div>
        );
    }
});

var MovieReview = React.createClass({
    getInitialState: function() {
        return {movies: []};
    },

    handleKeyUp: function() {
        var self = this,
            movieName = this.refs.search.getDOMNode().value;

        if (movieName) {
            $.ajax({
                url: "movie",
                type: "GET",
                contentType: "text/html; charset=utf-8",
                data: {name: movieName},
                success: function(resp) {
                    self.setState({movies: JSON.parse(resp)});
                }
            });
        } else {
            self.setState({movies: []});
        }
    },

    render: function() {
        return (
            <div>
                <div className="col-lg-7 input-group">
                    <input onKeyUp={this.handleKeyUp} ref="search" className="form-control" placeholder="Search movie" type="text" />

                    <span className="input-group-btn">
                        <button className="btn btn-info">
                            <span className="glyphicon glyphicon-search"></span>
                        </button>
                    </span>
                </div>

                <MovieList data={this.state.movies} />
            </div>
        );
    }
});

$(document).ready(function() {
    React.render(
        <MovieReview />,
        document.getElementById("app")
    );
});
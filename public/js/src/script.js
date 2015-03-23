var Movie = React.createClass({
    render: function () {
        return (
            <div className="movie" onClick={this.props.clickHandler}>
                <h3>{this.props.title} - {this.props.year}</h3>

                {this.props.children}
            </div>
        );
    }
});

var Review = React.createClass({
    render: function () {
        return (
            <div></div>
        );
    }
});

var MovieList = React.createClass({
    getReviews: function(movieId) {
        var self = this;

        $.ajax({
            type: "POST",
            url: "movie/review",
            contentType: "text/json",
            data: JSON.stringify({id: movieId}),
            success: function(resp) {
                self.setState({reviews: resp});
            }
        });
    },

    getInitialState: function() {
        return {reviews: []};
    },

    render: function () {
        var self = this,
            movies = this.props.data.map(function(movie) {
                var synopsis = "No synopsis.",
                    boundClick = self.getReviews.bind(self, movie.mId);

                if (movie.mSynopsis) {
                    synopsis = movie.mSynopsis;
                }

                return (
                    <Movie clickHandler={boundClick} title={movie.mTitle} year={movie.mYear}>
                        {synopsis}
                    </Movie>
                );
            });

        return (
            <div className="result_container">
                <div className="movie_list">
                    {movies}
                </div>

                <div className="review_list">
                    {this.state.reviews}
                </div>
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
                type: "GET",
                url: "movie",
                contentType: "text/html; charset=utf-8",
                data: {name: movieName},
                success: function(resp) {
                    // TODO check if result is from the same query in the search bar
                    self.setState({movies: JSON.parse(resp).sort(function(lhs, rhs) {return rhs.mYear - lhs.mYear;})});
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